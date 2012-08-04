(ns jat.junit
  (:use jat.junit
        clojure.test
        clojure.stacktrace))

(def junit3-test (java.lang.Class/forName "jamesr.tests.JUnit3TypeTest"))
(def junit4-test (java.lang.Class/forName "jamesr.tests.JUnit4TypeTest"))
(def junit3-fail-test (java.lang.Class/forName "jamesr.tests.JUnit3FailingTest"))
(def junit4-fail-test (java.lang.Class/forName "jamesr.tests.JUnit4FailingTest"))

(defn view-error [test]
  (print-stack-trace (:error test)))

(defn verify-test [junit-version junit-class junit-method-name test]
  (is (= junit-version (:junit-version test)))
  (is (= junit-class (:test-class test)))
  (is (= junit-method-name (.getName (:test-method test)))))

(deftest should-create-junit3-suite-with-correct-values []
  (let [tests (create-tests-from junit3-test)]
    (is (= 2 (count tests)))
    (verify-test :junit3 junit3-test "testSomething" (first tests))
    (verify-test :junit3 junit3-test "testSomethingAgain" (last tests))))

(deftest should-create-junit4-suite-with-correct-values []
  (let [tests (create-tests-from junit4-test)]
    (is (= 2 (count tests)))
    (verify-test :junit4 junit4-test "shouldDoSomething" (first tests))
    (verify-test :junit4 junit4-test "shouldDoSomethingAgain" (last tests))))

(deftest should-run-junit3-test []
  (let [test (first (create-tests-from junit3-test)) 
        result (run-test test)]
    (is (= :passed (:error-type result)))
    (is (nil? (:exception result)))))
    
(deftest should-run-junit4-test []
  (let [test (first (create-tests-from junit4-test)) 
        result (run-test test)]
    (is (= :passed (:error-type result)))
    (is (nil? (:exception result))))) 
           
(defn verify-failure [failure error-type error-message]
  (is (= error-type (:error-type failure)))
  (is (= error-message (.getMessage (:exception failure)))))

(deftest should-handle-failed-for-junit3-suite []
  (let [tests (create-tests-from junit3-fail-test)
        result-error (run-test (first tests))
        result-failure (run-test (last tests))]
    (verify-failure result-failure :fail "expected:<false> but was:<true>")
    (verify-failure result-error :error "A test error message")))

(deftest should-handle-failed-for-junit34suite []
  (let [tests (create-tests-from junit4-fail-test)
        result-error (run-test (first tests))
        result-failure (run-test (last tests))]
    (verify-failure result-failure :fail "expected:<false> but was:<true>")
    (verify-failure result-error :error "A test error message")))

