(ns clojuretwbot.db
  (:require [clojuretwbot.db.core       :as core]
            [clojuretwbot.db.migrations :as migrations]
            [taoensso.timbre :as timbre :refer [debug info warn error fatal]]))

(defn add-link
  "Add link to db."
  [link]
  (timbre/info "add link: " link "to database")
  (core/create-url! {:url link}))

(defn contains-link?
  "Check if link is saved in db."
  [link]
  (-> (core/get-url {:url link})
      first
      boolean))

(defn aa
  []
  (apply str  (core/list-all-url))
  )

;; (core/list-all-url)

(defn migrate [args]
  (migrations/migrate args))
