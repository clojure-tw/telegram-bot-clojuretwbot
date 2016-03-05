(ns clojuretwbot.db.core
  (:require [yesql.core  :refer [defqueries]]))

(def conn {:classname   "org.sqlite.JDBC"
           :subprotocol "sqlite"
           :subname     "./resources/database.db"
           :user        "clojuretwbot"
           :password    ""})

(defqueries "sql/queries.sql" {:connection conn})

;; (defn create!
;;   "Create default database if not exist."
;;   []
;;   (try
;;     (jdbc/db-do-commands db-spec
;;                          (jdbc/create-table-ddl :tweets
;;                                                 [:url :text]))

;;     (catch Exception e (println e))))
