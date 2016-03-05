(ns clojuretwbot.db.migrations
  (:require [migratus.core :as migratus]
            [clojuretwbot.db.core :as core]))

(defn parse-ids [args]
  (map #(Long/parseLong %) (rest args)))

(defn migrate [args]
  (let [config {:store :database
                :migration-dir "migrations"
                :db core/conn}]
    (case (first args)
      "migrate"
      (if (> (count args) 1)
        (apply migratus/up config (parse-ids args))
        (migratus/migrate config))
      "rollback"
      (if (> (count args) 1)
        (apply migratus/down config (parse-ids args))
        (migratus/rollback config)))))