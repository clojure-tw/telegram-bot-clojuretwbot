(ns clojuretwbot.core
  (:require [clojuretwbot.scheduler :as scheduler]
            [clojuretwbot.db :as db]))

;; real entry point
(defn start-app []
  ;; start the scheduler
  (scheduler/start))

(defn -main
  "The main function of clojuretwbot. When execute this with argument
  `migrate' or `rollback', this application will do database migration/roollback.
  You need to setup following environment variables to make this app work:

  TOKEN     : telegram bot token get from bot father
  CHAT_ID   : telegram chat room id bot will tweet to
  DATABASE  : the database path you want to save to, ex: resources/database.db"
  [& args]
  (cond (some #{"migrate" "rollback"} args)
        (do (db/migrate args) (System/exit 0))
        :else
        (start-app)))
