# pen-size-clojure
Just a dumb project that calculates "lines of code" per contributor 

Usage

```
git clone https://github.com/bugzmanov/pen-size-clojure.git
cd pen-size-clojure
lein uberjar
java -jar target/pen-size-1.0.0-standalone.jar https://github.com/<org>/<repo>.git
```

Example of output for apache/storm.git repository:


Can work with local repositories as well

```
java -jar target/pen-size-1.0.0-standalone.jar /Users/akakiy/projects/some_repo
```
