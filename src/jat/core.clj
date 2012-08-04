(ns jat.core (:require [jat.junit :as junit] [jat.db :as db] [jat.sort :as sort]))

(defrecord TestResult [buildnum run-time error-type exception duration])
(defrecord TestResults [test-class test-method results])
(defrecord Configuration [buildnum run-time test-classes])

(defn- item-name [x] (.getName x))

(defn load-results [test]
  (let [clazz (item-name (:test-class test)) 
        method (item-name (:test-method test))]
  (if (db/test-exists? clazz method)
    (db/load-test clazz method)
    (TestResults. clazz method ()))))

(defn store-results [old-test-result new-test-result]
  (if (db/test-exists? (:test-class old-test-result) (:test-method old-test-result))
    (db/update-test-results old-test-result new-test-result)
    (db/save-test-results new-test-result)))

(defn execute-test [test buildnum run-time]
  (let [old-test-result (load-results test)
        test-execution (junit/run-test test)
        latest-result (TestResult. buildnum run-time (:error-type test-execution) (:exception test-execution) 0)
        new-test-result (TestResults. (:test-class old-test-result) (:test-method old-test-result) (cons latest-result (:results old-test-result)))]
    (store-results old-test-result new-test-result)))
  
(defn execute [configuration]
  (let [tests (flatten (map junit/create-tests-from (:test-classes configuration)))
        buildnum (:buildnum configuration)
        curtime (:run-time configuration)]
    (map (fn [t] (execute-test t buildnum curtime)) tests)))
  

