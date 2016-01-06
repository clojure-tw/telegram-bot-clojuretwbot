(ns clojuretwbot.core
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.core.async :refer [chan go-loop >! <! put!] :as async]
            [feedparser-clj.core :as feed]
            [clojure.data :as data]
            [cronj.core :as cronj :refer [cronj]]
            [cemerick.url :refer [url]]
            [taoensso.timbre :as timbre :refer [debug info warn error fatal]]))

;; save config here in edn format
(def state (atom nil))

(defn valid-url?
  "Test whether a URL is valid, returning a map of information about it if
  valid, nil otherwise."
  [url-str]
  (try
    (url url-str)
    (catch Exception _ nil)))

;; read the config file
(defn parse-config
  "Parse user config file, all config in edn format."
  [url]
  (edn/read-string (slurp url)))

(defn send-message!
  ([message] (send-message! @state message))
  ([{:keys [chat-id token]} message]
   (http/post (str "https://api.telegram.org/bot" token "/sendMessage")
              {:content-type :json
               :form-params {:chat_id chat-id
                             :text message}})
   (timbre/info (str "send-message! with :token " token " :chat-id " chat-id " :message " message))))

;; Feed parser
(defn get-feed-url
  ([] (get-feed-url (:feed-uri @state) (:feed-archive @state)))
  ([feed-uri feed-archive]
   (let [atom-feed (feed/parse-feed feed-uri)
         archive (edn/read-string (slurp feed-archive))
         new-item (first (data/diff (map :uri (:entries atom-feed)) archive))]
     (spit feed-archive
           (with-out-str (pr (distinct (concat new-item archive)))))
     new-item)))

(defn tweet-to-telegram
  "Check if new feed exist, send url to telegram if yes."
  ([] (tweet-to-telegram nil nil))
  ([_ _]
   (timbre/info "tweet to telegram trigger!!")
   (let [feed (reverse (get-feed-url))]
     (doseq [f feed]
       (if (valid-url? f)
         (send-message! (str f)))))))

(def scheduler
  (cronj :entries
         [{:id "tweet-to-telegram"
           :handler tweet-to-telegram
           :schedule "0 /30 * * * * *"   ; every 30minute
           ;;:schedule "/2 * * * * * *"   ; every 2s
           }]))

;; real entry point
(defn start-app [config]
  ;; we only modify state once
  (reset! state config)
  ;; before run scheduler, we run it first time to update db
  (tweet-to-telegram)
  ;; start the scheduler
  (cronj/start! scheduler)
  (println "start scheduler for checking planet.clojure"))

;; load config file and parse it
(defn -main [& args]
  (let [arg1 (nth args 0)]
    (if arg1
      (-> (parse-config arg1) (start-app))
      (println "ERROR: Please specify config file."))))
