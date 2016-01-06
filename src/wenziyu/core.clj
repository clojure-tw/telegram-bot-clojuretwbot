(ns wenziyu.core
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.core.async :refer [chan go-loop >! <! put!] :as async]
            [feedparser-clj.core :as feed]
            [clojure.data :as data]))

;; Simple example for testing
(comment
  (count
   "This is a simple example you can test for this bot, if you type more than
   256 words, this bot will show some WTF image to let you or other peoples in
   the same chat group konw what you say is too hard to read. We don't
   want to see too long chat in our im message."
   )

  (count
   "今天早上被人嚇到，決定寫這個bot來壓壓驚，我們都知道寫文章一定要分段落，講話
   也是，全部擠在一起實在讓人看不太懂。為了解決這個問題，我開發一個telegram bot
   會去監控是不是有人講話講的太誇張，太誇張的話就會讓這隻bot顯示WTF 系列圖片。所
   以大家看到這樣的圖片就會知道自己實在太誇張了，不過這不是為了避免文字獄，我正
   在努力亂掰一些訊息，反正就是只要你的訊息超過了256，這隻 bot就會開始運作，貼上
   新的圖片給你看。我的天啊 256字有夠他媽的難掰，我繼續掰掰掰掰，這隻bot晚點整理
   好後會放到clojure-tw repo ，總之就是好玩就好，現在到底幾行文字了啊啊啊啊啊啊。"
   ))

;; save config here in edn format
(def state (atom nil))

;; a channel to queue input messages
(def channel (chan))

;; read the config file
(defn parse-config
  "Parse user config file, all config in edn format."
  [url]
  (edn/read-string (slurp url)))

(defn send-sticker!
  "Send sticker image to telegram, the sticker image is place in resource
  directory and start from wtf0.jpg, wtf1.jpg, ... wtf100.jpg."
  [chat-id]
  (let [token (@state :token)
        sticker-range (@state :sticker-range)
        img (str "wtf" (rand-int sticker-range) ".jpg")]
    (http/post (str "https://api.telegram.org/bot" token "/sendSticker")
               {:multipart [{:name "chat_id" :content (str chat-id)}
                            {:name "sticker" :content (clojure.java.io/file (clojure.java.io/resource img))}]}
               )))

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
            (send-sticker! chat-id)))))
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

;; Feed parser
(defn get-feed-url
  [feed-uri feed-archive]
  (let [atom-feed (feed/parse-feed feed-uri)
        archive (edn/read-string (slurp feed-archive))
        new-item (first (data/diff (map :uri (:entries atom-feed)) archive))]
    (spit feed-archive
          (with-out-str (pr (distinct (concat new-item archive)))))
    new-item))
