(ns jat.junit (:use clojure.stacktrace))

(defrecord Test [test-class test-method junit-version])

(defrecord Failure [error-type exception duration])

(defn- junit-version [junit-class]
  "Detects whether the unit test is junit 3 or junit 4 and returns the resulting keyword."
  (if (. (Class/forName "junit.framework.TestCase") isAssignableFrom junit-class)
    :junit3
    :junit4))

(defn- has-annotation [annotation method] 
  "Checks for an annotation on a java method."
  (. method getAnnotation annotation))

(def is-junit4-test (partial has-annotation org.junit.Test))

(def is-junit4-setup (partial has-annotation org.junit.Before))

(def is-junit4-teardown (partial has-annotation org.junit.After))

(defn- has-zero-arguments [method]
  "Checks that a method has no arguments."
  (= (java.lang.reflect.Array/getLength (. method getParameterTypes)) 0))

(defn- is-junit3-method [name-filter method]
  (and (has-zero-arguments method) (name-filter (. method getName))))

(def is-junit3-test (partial is-junit3-method (fn [name] (. name startsWith "test"))))

(def is-junit3-setup (partial is-junit3-method (fn [name] (. name equals "setUp"))))

(def is-junit3-teardown (partial is-junit3-method (fn [name] (. name equals "tearDown"))))

(defn- methods 
  "Returns all methods on the test suite class based on a filter."
  ([junit-class]
  (methods identity junit-class))
  ([f junit-class]
  (filter f (. junit-class getMethods))))

(defmulti test-methods :version)
(defmethod test-methods :junit4 [junit-map] (methods is-junit4-test (:junit-class junit-map)))
(defmethod test-methods :junit3 [junit-map] (methods is-junit3-test (:junit-class junit-map)))

(defn create-tests-from [junit-class]
  "Given a class, create a set of tests from it."
  (let [junit-version (junit-version junit-class) 
        test-methods (test-methods {:version junit-version :junit-class junit-class})]
    (map (fn [m] (Test. junit-class m junit-version)) test-methods)))

(defn- invoke-methods [instance methods] 
  "Invoke the required methods on the given class instance."
  (map #(. % invoke instance) methods))

(defmulti invoke-setup-methods (fn [test inst] (:junit-version test)))
(defmethod invoke-setup-methods :junit4 [test inst] 
  (invoke-methods inst (methods is-junit4-setup (:test-class test))))
(defmethod invoke-setup-methods :junit3 [test inst] 
  (invoke-methods inst (methods is-junit3-setup (:test-class test))))

(defmulti invoke-teardown-methods (fn [test inst] (:junit-version test)))
(defmethod invoke-teardown-methods :junit4 [test inst] 
  (invoke-methods inst (methods is-junit4-teardown (:test-class test))))
(defmethod invoke-teardown-methods :junit3 [test inst] 
  (invoke-methods inst (methods is-junit3-teardown (:test-class test))))

(defn duration-since [starttime]
  (- (java.lang.System/currentTimeMillis) starttime))

(defn- handle-error [test exception starttime]
  "Handle a test error and decide whether the test failed or errored"
  (let [actualexception (root-cause exception)]
    (println "Got error in test " (:method test))
    (print-stack-trace actualexception)
    (cond
      (= (java.lang.Class/forName "junit.framework.AssertionFailedError")  (. actualexception getClass)) 
      (Failure. :fail actualexception (duration-since starttime))
      (= (java.lang.Class/forName "java.lang.AssertionError") (. actualexception getClass)) 
      (Failure. :fail actualexception (duration-since starttime))
      :else 
      (Failure. :error actualexception (duration-since starttime)))))

(defn run-test [test]
  "Runs the given test ensuring it is correctly set up and torn down."
  (let [starttime (java.lang.System/currentTimeMillis) 
        instance (.newInstance (:test-class test))
        test-args (make-array Object 0)]
	  (try
	    (do
	      (invoke-setup-methods test instance)
	      (. (:test-method test) invoke instance test-args)
	      (invoke-teardown-methods test instance)
           (Failure. :passed nil (duration-since starttime)))
	    (catch java.lang.Throwable error (handle-error test error starttime)))))
    

