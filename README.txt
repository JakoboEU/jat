JAT - JUnit Analysis Tool
--------------------------------------
Author: JamesR
URL: https://github.com/SheepCoder/jat

JAT is intended to be a tool for running Java JUnit tests.
It collects data about the tests that have been run and stores this in a mongo DB.
JAT can then use the data from JAT to optimise the order that unit tests are run in.
i.e. you can run the last failed tests first or the most failed tests first or the fastest tests first.
Tests are run in parallel.

The idea of this project came from a talk by Vladimir Sneblic at Thoughtworks in Manchester.
In addition the lack of development on JUnit Analysis Server (http://www.junit.org/node/601) provided some motivation.

Introduction to Code
------------------------------
The main entry point is the jat namespace (work backwards from the jat.core/execute function).
A test is a single test method.
A set of historical test results are stored in mongo.

Setting Up JAT
------------------------

1) Download and build the JAT test data
https://github.com/SheepCoder/jat-testdata

2) Download and install MongoDB
http://www.mongodb.org/downloads

3) Run mongo DB server
home$ mongod

4) Install JAT using leiningen
home$ lein install

5) Run JAT tests
home$ lein test

Using JAT in the Real World
--------------------------------------------
The following shows how to call the execute function with the correct configuration:

(def junit3-test (java.lang.Class/forName "jamesr.tests.JUnit3TypeTest"))
(def junit4-test (java.lang.Class/forName "jamesr.tests.JUnit4TypeTest"))

(def config (Configuration. "t1" 123456 [junit3-test junit4-test]))

(execute config)

See the core namespace for the configuration record.

Work Todo
------------------
The current todo list looks a bit like this... [some of these tasks are extremely vague]
2) DB Layer, extract protocol, default implementation to write to file
3) Sort out tests so they don't require mongo db, use dummy implementation of protocol.
4) Allow different DBMSs via Protocol
5) Reimplement sorting the run order of tests
6) Add additional sorting algorithms for ordering tests (average duration, total number of failures)
7) A pretty web front end for viewing results
8) Sort out logging test results out (along with any output from tests). e.g. in similar way to JUnit
9) Wrapping whole tool up in an ANT task (and/or maven plugin) to allow for use in real projects.
10) How are tests run in parallel? Think about extracting execution strategy.
11) Can this be extended to run clojure.test tests?
