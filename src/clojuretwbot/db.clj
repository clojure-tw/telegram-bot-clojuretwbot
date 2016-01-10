(ns clojuretwbot.db)

(defonce database
  (atom {:link #{}                      ; link we pused to telegram
         }))

(defn add-link [link]
  (swap! database assoc :link (into #{} (merge (vec (:link @database)) link))))

(defn contains-link? [link]
  (contains? (:link @database) link))
