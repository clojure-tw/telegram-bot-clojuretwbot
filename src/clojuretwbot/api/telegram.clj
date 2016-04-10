(ns clojuretwbot.api.telegram
  (:require [clj-http.client :as http]
            [environ.core :as environ :refer [env]]
            [taoensso.timbre :as timbre]))

;; telegram token from `TOKEN' environment vairable
(def ^:private token (env :token))

;; telegram token from `CHAT_ID' environment vairable
(def ^:private chat-id (env :chat-id))

(defn send-message!
  ([message] (send-message! message nil))
  ([message params]
   (http/post (str "https://api.telegram.org/bot" token "/sendMessage")
              {:content-type :json
               :form-params (merge {:chat_id chat-id
                                    :parse_mode "HTML"
                                    :text message} params)})
   (timbre/info (str "send-message! with :token " token " :chat-id " chat-id " :message " message))))