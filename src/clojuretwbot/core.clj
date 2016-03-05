(ns clojuretwbot.core
  (:require [clj-http.client    :as http]
            [clojure.data.json  :as json]
            [clojure.edn        :as edn]
            [clojure.core.async :as async :refer [chan go-loop >! <! put!]]
            [clojure.data       :as data]
            [cronj.core         :as cronj :refer [cronj]]
            [taoensso.timbre    :as timbre]
            ;; backends
            [clojuretwbot.db   :as db]
            [clojuretwbot.feed :as feed]))

;; save config here in edn format
(def state (atom nil))

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

(def scheduler
  (cronj :entries
         [;; every 30minute
          {:id "feed-fetcher"
           :handler (fn [_ _] (feed/fetch-all))
           :schedule "0 /1 * * * * *"}]))

(defn feeds-dispatcher []
  (go-loop []
    (let [{:keys [title link description] :as ch} (<! feed/channel)]
      ;; check if this link is already store in db, if not push to telegram
      (timbre/info "tweet to telegram trigger!!")
      (when (not (db/contains-link? link))
        (println (str "dispatch " link))
        (if (nil? description) ; some link can't be previewed by telegram
          (send-message! (str title "\n" link) {:disable_web_page_preview true})
          (send-message! (str link)))
        ;; Add link to db
        (db/add-link link)))
    (recur)))


(defn event-loop []
  (feeds-dispatcher))

;; real entry point
(defn start-app [config]
  ;; we only modify state once
  (reset! state config)
  ;; start event loop for listen events
  (event-loop)
  ;; start the scheduler
  (cronj/start! scheduler)
  (println "start scheduler for checking planet.clojure"))

;; load config file and parse it
(defn -main [& args]
  (cond (some #{"migrate" "rollback"} args)
        (do (db/migrate args) (System/exit 0))
        :else
        (let [arg1 (nth args 0)]
          (if arg1
            (-> (parse-config arg1) (start-app))
            (println "ERROR: Please specify config file.")))))

;; migration from old archive.edn
(comment
  (let [a (-> (slurp "archive.edn")
              edn/read-string
              vec)
        l (count a)]
    (loop [i 1]                           ; index 0 is nil
      (when (< i l)
        ;; (println (nth a i))
        (db/add-link (nth a i))
        (recur (inc i))))))
