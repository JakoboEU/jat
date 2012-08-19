(ns jat.test.core
  (:use 
    jat.core 
    jat.junit
    clojure.test))

(def junit4-test (java.lang.Class/forName "jamesr.tests.JUnit4TypeTest"))

(defn mock-load-results [test] 
  (jat.core.TestResults. "no-class" "no-method" 9999 :errored ()))

(defn mock-store-results [old-test new-test]
  new-test)

(deftest should-execute-tests-and-append-result []
        (binding [jat.core/load-results mock-load-results
                  jat.core/store-results mock-store-results]
          (let [test (first (create-tests-from junit4-test))
                test-result (execute-test test "build" 12345)]
            (is (= 1 (count (:results test-result))))
            (is (= "no-class" (:test-class test-result)))
            (is (= "no-method" (:test-method test-result)))
            (is (= :passed (:last-error-type test-result)))
            (let [test-run-details (first (:results test-result))]
              (is (= 12345 (:run-time test-run-details)))
              (is (= "build" (:buildnum test-run-details)))
              (is (= :passed (:error-type test-run-details)))
              (is (<= 0 (:duration test-run-details)))
              (is (= (:duration test-run-details) (:last-duration test-result)))))))
         
