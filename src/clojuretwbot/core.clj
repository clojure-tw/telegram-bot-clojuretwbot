(ns clojuretwbot.core
  (:require [clj-http.client    :as http]
            [taoensso.timbre    :as timbre]
            [hara.io.scheduler  :as sch]
            [environ.core       :as environ :refer [env]]
            [clojure.core.async :as async   :refer [chan go-loop >! <! put!]]
            [clojuretwbot.db    :as db]
            [clojuretwbot.feed  :as feed]))

(defn valid-link?
  "Check if url is valid link. We hate 404 error."
  [url]
  (try (= 200
          (-> (http/get url  {:insecure? true})
              :status))
       (catch Exception e
         false)))

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
  (sch/scheduler
   ;; every 30 min send feeds link
   {:feed-fetcher {:handler (fn [_ _] (feed/fetch-all))
                   :schedule "0 /30 * * * * *"
                   :params nil}
    }))

(defn feeds-dispatcher []
  (go-loop []
    (let [{:keys [title link description] :as ch} (<! feed/channel)]
      ;; check if this link is already store in db, if not push to telegram
      ;; if link is not valid, ignore it.
      (when (and (not (db/contains-link? link))
                 (valid-link? link))
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
  (sch/start! scheduler)
  (timbre/info "start scheduler for clojuretwbot."))

(defn -main
  "The main function of clojuretwbot. When execute this with argument
  `migrate' or `rollback', this application will do database migration/roollback.
  You need to setup following environment variables to make this app work:

  TOKEN     : telegram bot token get from bot father
  CHAT_ID   : telegram chat room id bot will tweet to
  DATABASE  : the database path you want to save to, ex: resources/database.db"
  [& args]
  (cond (some #{"migrate" "rollback"} args)
        (do (db/migrate args) (System/exit 0))
        :else
        (start-app)))
