(ns jat.db (:require [somnium.congomongo :as congo]))

(def connection (congo/make-connection "junit"))

(defn load-test 
  "Loads the suite with the given name from mongo"
  [test-class test-method]
  (congo/with-mongo connection
    (congo/fetch-one :testresults
               :where {:test-class test-class
                       :test-method test-method})))

(defn test-exists? 
  "Checks to see if the suite with the given name exists already"
  [test-class test-method]
  (congo/with-mongo connection
    (< 0 (congo/fetch-count :testresults
                 :where {:test-class test-class
                       :test-method test-method}))))

(defn save-test-results
  "Save the given suite, assumes it is new"
  [test-results]
  (congo/with-mongo connection
                    (congo/insert! :testresults test-results)))

(defn update-test-results 
  "Updates an existing suite"
  [oldresults newresults]
  (congo/with-mongo connection
                    (congo/update! :testresults oldresults newresults)))

