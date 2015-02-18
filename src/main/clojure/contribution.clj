(ns contribution
  (:import [java.io File]
           [org.eclipse.jgit.lib Repository Constants EmptyProgressMonitor]
           (org.eclipse.jgit.blame BlameGenerator BlameResult)
           (org.eclipse.jgit.api Git CloneCommand)
           (org.eclipse.jgit.treewalk TreeWalk)
           (org.eclipse.jgit.revwalk RevWalk)
           (org.eclipse.jgit.transport UsernamePasswordCredentialsProvider)
           )
  )

(defn load-repo [src]
  (cond
    (= String (class src)) (load-repo (File. src))
    (= File (class src))
       (let [builder (Git/open src)]
         (.getRepository builder)
         )))

(defn blame-file [^Repository repo ^String path]
  (let [blame (BlameGenerator. repo path)
        head (.resolve repo Constants/HEAD)]
    (.push blame nil head)
    (BlameResult/create blame)))

(defn compute-blame [^BlameResult br]
  (loop [idx (.computeNext br)
         map {}]
    (if (= -1 idx)
      map
      (let [length (.lastLength br)
            person (.getSourceAuthor br idx)
            person-name (.getName person)
            person-prev (get map person-name 0)
            result-map (assoc map person-name (+ person-prev length))]
        (recur (.computeNext br) result-map))
      )))

(defn walk-directory [^Repository repo]
  (let [tree-walk (TreeWalk. repo)
        head (.resolve repo Constants/HEAD)
        commit (.parseCommit (RevWalk. repo) head)]
    (.setRecursive tree-walk false)
    (.addTree tree-walk (.getTree commit))
    (loop [result []]
      (if (.next tree-walk)
        (if (.isSubtree tree-walk)
          (do (.enterSubtree tree-walk) (recur result))
          (recur (conj result (.getPathString tree-walk))))
        (vec result))
      )))

(defn blame-local-repo [repo-path]
  (println "Calculating stats")
  (let [repo (if (string? repo-path) (load-repo repo-path) repo-path)
        files (walk-directory repo)
        local-path (fn [file] (.replaceAll file (-> repo (.getDirectory) (.getPath)) ""))
        make-blame (fn [file]
                     (let [blame-result (blame-file repo (local-path file))]
                       (if (nil? blame-result) {} (compute-blame blame-result))
                       ))]
    (apply merge-with + (map #(make-blame %1) files))
    ))

(defn clone-remote-repo
  ([url location creds] (clone-remote-repo url location "master" creds))
  ([url location branch creds]
   (println "Clonning repo")
   (let [command (-> (CloneCommand.) (.setURI url) (.setDirectory location) (.setBranch branch))
         [username password] creds]
     (.call
       (if (nil? creds) command (.setCredentialsProvider command (UsernamePasswordCredentialsProvider. username password)))))))

(defn pen-size [percent]
  (str (apply str (repeat (max (int (/ percent 2)) 1) "=")) "з"))

(defn normalize-str [string length side]
  (let [spaces (apply str (repeat (- length (.length string)) " "))]
    (if (= side :right)
      (str spaces string)
      (str string spaces)
      )))

(defn print-blame [repo]
  (let [blame-map (blame-local-repo repo)
        total-lines (reduce-kv #(+ %1 %3) 0 blame-map)
        sorted-map (into (sorted-map-by (fn [key1 key2]
                                          (compare [(get blame-map key2) key2]
                                                   [(get blame-map key1) key1])))
                         blame-map)
        percent (fn [value] (int (* (/ value total-lines) 100)))]
    (println "")
    (doseq [[k v] (map identity sorted-map)]
      (println (normalize-str k 25 :left) " | " (normalize-str (str v) 5 :right) " | " (pen-size (percent v)) " " (percent v) "%"))
    (println "\n Total lines: " total-lines)
    ))

(defn delete-recursively [fname]
  (let [func (fn [func f]
               (when (.isDirectory f)
                 (doseq [f2 (.listFiles f)]
                   (func func f2)))
               (clojure.java.io/delete-file f))]
    (func func (clojure.java.io/file fname))))

(defn measure-remote-repo
  ([url branch] (measure-remote-repo url branch nil))
  ([url branch creds]
   (let [repo-name (nth (re-find #".*/(.*)\.git" url) 1)
         tmp-dir (if (nil? repo-name) (File. url) (File. repo-name))]
     (if (.exists tmp-dir)
       (print-blame (load-repo tmp-dir))
       (try
         (print-blame (.getRepository (clone-remote-repo url tmp-dir branch creds)))
       (finally
         (delete-recursively tmp-dir)
         )))
    )))