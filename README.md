# pen-size-clojure
Just a simple clojure project that calculates "lines of code" per contributor. I'm using it mostly to undertand how many lines of code of authors who started the project are stil active in master ) 

Usage

```
git clone https://github.com/bugzmanov/pen-size-clojure.git
cd pen-size-clojure
lein uberjar

java -jar target/pen-size-1.0.0-standalone.jar https://github.com/<org>/<repo>.git  [branch_name]
```

Example of output for apache/storm.git repository:

```
java -jar target/pen-size-1.0.0-standalone.jar https://github.com/apache/storm.git master
```

![pen-size-screenshot](https://cloud.githubusercontent.com/assets/502482/6257227/8797d8ba-b78c-11e4-9f82-7410d07172d4.png)

Can work with local repositories as well

```
java -jar target/pen-size-1.0.0-standalone.jar /Users/akakiy/projects/some_repo
```
