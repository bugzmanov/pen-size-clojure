(ns contribution.main
  (:require contribution)
  (:gen-class :main true)
  (:import (org.eclipse.jgit.api.errors TransportException)
           (java.io Console)))

(defn -main
  "The application's main function"
  [& args]
  (let [branch (if (= 2 (count args)) (second args) "master")]
    (try
      (contribution/measure-remote-repo (first args) branch)
      (catch TransportException e
        (
          (println "Authentication is required!")
          (let [^Console console (System/console)
                username (.readLine console "Username:" (to-array []))
                pwd (.readPassword console "Password: " (to-array []))]
            (contribution/measure-remote-repo (first args) branch [username pwd])
            )))
      )
    ))