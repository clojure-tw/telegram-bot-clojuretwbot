(ns clojuretwbot.db
  (:require [clojuretwbot.db.core       :as core]
            [clojuretwbot.db.migrations :as migrations]
            [taoensso.timbre :as timbre :refer [debug info warn error fatal]]))

(defn add-link
  "Add link to db."
  [link]
  (timbre/info "add link: " link "to database")
  (core/add-url! {:url link}))

(defn contains-link?
  "Check if link is saved in db."
  [link]
  (-> (core/find-url {:url link})
      first
      boolean))

(defn migrate [args]
  (migrations/migrate args))
