(ns clojuretwbot.db.core-test
  (:require [clojuretwbot.db.core :as db]
            [clojuretwbot.db.migrations :as migrations]
            [clojure.java.jdbc :as jdbc]
            [clojure.test :refer :all]))

(use-fixtures
  :once
  (fn [f]
    (migrations/migrate ["migrate"])
    (f)))

(deftest test-database
  (jdbc/with-db-transaction [t-conn db/conn]
    (jdbc/db-set-rollback-only! t-conn)
    (is (= 1 (db/create-url!
              {:url "http://clojure.tw"}
              {:connection t-conn})))
    (is (= [{:url "http://clojure.tw"}]
           (db/get-url {:url "http://clojure.tw"} {:connection t-conn})))))