(ns clojuretwbot.core
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.core.async :refer [chan go-loop >! <! put!] :as async]
            [feedparser-clj.core :as feed]
            [clojure.data :as data]))

;; save config here in edn format
(def state (atom nil))

;; a channel to queue input messages
(def channel (chan))

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

(defn receive-message
  [offset]
  (let [token (@state :token)
        req (-> (http/get (str "https://api.telegram.org/bot" token "/getUpdates")
                          {:query-params {:offset offset}})
                :body
                (json/read-str :key-fn keyword))
        result (-> req :result)]

    (println (str ">>> " req))        ; for debug

    ;; when event come, send it to channel
    (when-not (and (nil? result) (zero? offset))
      (put! channel result))

    ;; return update_id
    ;; An update is considered confirmed as soon as getUpdates is called with an
    ;; offset higher than its update_id.
    (if-let [ofs (-> result last :update_id)]
      (inc ofs) 0)))

;; As busy loop to listen and dispatch telegram events
(defn- listen-telegram-events []
  (println "listen bot ")
  (loop [offset 0]
    ;; sleep for prevent busy polling
    (Thread/sleep (@state :interval))
    ;; tail recursive loop
    (recur (receive-message offset))))

;; Start a core.async event-loop to handle sending sticker event
(defn event-loop []
  (go-loop []
    (doseq [c (<! channel)]
      (let [message (-> c :message)
            text    (-> message :text)
            chat-id (-> message :chat :id)]

        (println (str "---> " c))       ; for debug

        (when-not (nil? text)
          ;; if user say more than 300 word, send a wtf sticker to group
          (when (< (@state :threshold) (count text))
            ;; FIXME: shoud we send message here ?
            ))))
    (recur)))

;; real entry point
(defn start-app [config]
  ;; we only modify state once
  (reset! state config)
  ;; listen events
  (event-loop)
  ;; listen telegram event
  (listen-telegram-events))

;; load config file and parse it
(defn -main [& args]
  (let [arg1 (nth args 0)]
    (if arg1
      (-> (parse-config arg1) (start-app))
      (println "ERROR: Please specify config file."))))
