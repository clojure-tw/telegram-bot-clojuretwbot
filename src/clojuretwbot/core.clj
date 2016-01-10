(ns clojuretwbot.core
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.core.async :refer [chan go-loop >! <! put!] :as async]
            [feedparser-clj.core :as feed]
            [clojure.data :as data]
            [cronj.core :as cronj :refer [cronj]]
            [cemerick.url :refer [url]]
            [taoensso.timbre :as timbre :refer [debug info warn error fatal]]
            ;; backends
            [clojuretwbot.db :as db]
            [clojuretwbot.backend.google-groups :as google-groups]))

;; save config here in edn format
(def state (atom nil))

;; store init status
(def status (atom {:init false}))

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
  ([message] (send-message! @state message nil))
  ([message params] (send-message! @state message params))
  ([{:keys [chat-id token]} message params]
   (http/post (str "https://api.telegram.org/bot" token "/sendMessage")
              {:content-type :json
               :form-params (merge {:chat_id chat-id
                                    :text message} params)})
   (timbre/info (str "send-message! with :token " token " :chat-id " chat-id " :message " message))))

;; Feed parser
(defn get-feed-url
  ([] (get-feed-url (:feed-uri @state) (:feed-archive @state)))
  ([feed-uri feed-archive]
   (let [atom-feed (map :link (:entries (feed/parse-feed feed-uri)))
         archive (edn/read-string (slurp feed-archive))
         ;; new-item (first (data/diff (map :uri (:entries atom-feed)) archive))
         new-item (into #{} (map (fn [x] (if-not (contains? archive x) x nil)) atom-feed))]
     (spit feed-archive
           (with-out-str (pr (into #{} (concat new-item archive)))))
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
           }
          {:id "clojure-mailing-list"
           :handler google-groups/find-ANN-in-clojure-list
           :schedule "0 /30 * * * * *"  ; every 30minute
           }]))

(defn mailing-list-dispatcher
  []
  (go-loop []
    (let [{:keys [title link] :as ch} (<! google-groups/channel)]
      ;; check if this link is already store in db, if not push to telegram
      (when (and (not (db/contains-link? link)) (:init @status))
        (send-message! (str title "\n" link) {:disable_web_page_preview true}))
      ;; Add link to db
      (db/add-link link))
    (recur)))

(defn init! []
  (tweet-to-telegram)
  (google-groups/find-ANN-in-clojure-list)
  ;; We finish initialize
  (Thread/sleep (* 10 1000))            ; FIXME: not a good method, we need to wait channel first init
  (swap! status assoc :init true))

(defn event-loop []
  (mailing-list-dispatcher))

;; real entry point
(defn start-app [config]
  ;; we only modify state once
  (reset! state config)
  ;; start event loop for listen events
  (event-loop)
  ;; before run scheduler, we run it first time to update db
  (init!)
  ;; start the scheduler
  (cronj/start! scheduler)
  (println "start scheduler for checking planet.clojure"))

;; load config file and parse it
(defn -main [& args]
  (let [arg1 (nth args 0)]
    (if arg1
      (-> (parse-config arg1) (start-app))
      (println "ERROR: Please specify config file."))))
