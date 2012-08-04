(ns jat.test.core
  (:use 
    jat.core 
    jat.junit
    clojure.test))

(def junit4-test (java.lang.Class/forName "jamesr.tests.JUnit4TypeTest"))
(def junit3-fail-test (java.lang.Class/forName "jamesr.tests.JUnit3FailingTest"))
