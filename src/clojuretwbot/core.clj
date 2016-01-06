(ns clojuretwbot.core
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.core.async :refer [chan go-loop >! <! put!] :as async]
            [feedparser-clj.core :as feed]
            [clojure.data :as data]))

;; save config here in edn format
(def state (atom nil))

;; read the config file
(defn parse-config
  "Parse user config file, all config in edn format."
  [url]
  (edn/read-string (slurp url)))

;; Feed parser
(defn get-feed-url
  [feed-uri feed-archive]
  (let [atom-feed (feed/parse-feed feed-uri)
        archive (edn/read-string (slurp feed-archive))
        new-item (first (data/diff (map :uri (:entries atom-feed)) archive))]
    (spit feed-archive
          (with-out-str (pr (distinct (concat new-item archive)))))
    new-item))

;; real entry point
(defn start-app [config]
  ;; we only modify state once
  (reset! state config)
  )

;; load config file and parse it
(defn -main [& args]
  (let [arg1 (nth args 0)]
    (if arg1
      (-> (parse-config arg1) (start-app))
      (println "ERROR: Please specify config file."))))
