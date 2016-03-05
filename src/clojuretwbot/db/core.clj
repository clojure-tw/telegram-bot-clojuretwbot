(ns clojuretwbot.db.core
  (:require [yesql.core   :refer [defqueries]]
            [environ.core :refer [env]]))

(def conn {:classname   "org.sqlite.JDBC"
           :subprotocol "sqlite"
           :subname     (env :database)
           :user        "clojuretwbot"
           :password    ""})

(defqueries "sql/queries.sql" {:connection conn})