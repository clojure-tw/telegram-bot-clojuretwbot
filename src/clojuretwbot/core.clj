(ns clojuretwbot.core
  (:require [clojuretwbot.scheduler :as scheduler]
            [clojuretwbot.db :as db]
            [clojuretwbot.config :refer [env]]
            [taoensso.timbre :as timbre]
            [mount.core :as mount]))

(mount/defstate ^{:on-reload :noop}
  schele-server
  :start (scheduler/start)
  :stop  (scheduler/stop))

(defn stop-app []
  (doseq [component (:stopped (mount/stop))]
    (timbre/info component "stopped"))
  (shutdown-agents))

(defn start-app []
  (doseq [component (:started (mount/start))]
    (timbre/info component "started"))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

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
