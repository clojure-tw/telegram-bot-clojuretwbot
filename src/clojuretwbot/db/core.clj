(ns clojuretwbot.db.core
  (:require [yesql.core  :refer [defqueries]]))

(def conn {:classname   "org.sqlite.JDBC"
           :subprotocol "sqlite"
           :subname     "./resources/database.db"
           :user        "clojuretwbot"
           :password    ""})

(defqueries "sql/queries.sql" {:connection conn})