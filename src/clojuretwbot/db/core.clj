(ns clojuretwbot.db.core
  (:require [yesql.core   :refer [defqueries]]
            [coldnew.config :refer [conf]]))

(def conn {:classname   "org.sqlite.JDBC"
           :subprotocol "sqlite"
           :subname     (conf :database)
           :user        "clojuretwbot"
           :password    ""})

(defqueries "sql/queries.sql" {:connection conn})