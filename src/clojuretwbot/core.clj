(ns clojuretwbot.core
  (:require [clj-http.client    :as http]
            [clojure.data.json  :as json]
            [clojure.edn        :as edn]
            [clojure.data       :as data]
            [taoensso.timbre    :as timbre]
            [cronj.core         :as cronj   :refer [cronj]]
            [environ.core       :as environ :refer [env]]
            [clojure.core.async :as async   :refer [chan go-loop >! <! put!]]
            ;; backends
            [clojuretwbot.db     :as db]
            [clojuretwbot.feed   :as feed]))

;; Environment Variables
;; TOKEN      ; telegram bot token get from bot father
;; CHAT_ID    ; chat_id we want to send info to
;; DATABASE   ; database file position

(defn send-message!
  ([message] (send-message! message nil))
  ([message params]
   (let [token   (env :token)
         chat-id (env :chat-id)]
     (http/post (str "https://api.telegram.org/bot" token "/sendMessage")
                {:content-type :json
                 :form-params (merge {:chat_id chat-id
                                      :parse_mode "HTML"
                                      :text message} params)})
     (timbre/info (str "send-message! with :token " token " :chat-id " chat-id " :message " message)))))

(def scheduler
  (cronj :entries
         [;; every 30minute
          {:id "feed-fetcher"
           :handler (fn [_ _] (feed/fetch-all))
           :schedule "0 /30 * * * * *"}]))

(defn feeds-dispatcher []
  (go-loop []
    (let [{:keys [title link description] :as ch} (<! feed/channel)]
      ;; check if this link is already store in db, if not push to telegram
      (timbre/info "tweet to telegram trigger!!")
      (when (not (db/contains-link? link))
        (println (str "dispatch " link))
        (if (nil? description) ; some link can't be previewed by telegram
          (send-message! (str "<b>" title "</b>\n" link) {:disable_web_page_preview true})
          (send-message! (str link)))
        ;; Add link to db
        (db/add-link link)))
    (recur)))


(defn event-loop []
  (feeds-dispatcher))

;; real entry point
(defn start-app []
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
        (start-app)))
