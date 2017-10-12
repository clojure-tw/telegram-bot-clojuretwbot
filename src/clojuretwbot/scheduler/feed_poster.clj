(ns clojuretwbot.scheduler.feed-poster
  (:require [clj-http.client :as http]
            [clojure.core.async :as async :refer [<! chan go-loop put! >! go timeout]]
            [clojure.string :as str]
            [clojuretwbot.api.telegram :as telegram]
            [clojuretwbot.db :as db]
            [feedparser-clj.core :as feedparser]
            [net.cgrand.enlive-html :as html]
            [taoensso.timbre :as timbre]))

;; channel that store new feed info
(def ^:private channel (chan))

(defn- valid-link?
  "Check if url is valid link. We hate 404 error."
  [url]
  (try (= 200
          (-> (http/get url  {:insecure? true})
              :status))
       (catch Exception e
         false)))

;; since we'll send feed url to telegram, some feed doesn't has og:drecription tag
(defn- fetch-html
  "Fetch the HTML source of link, return nil when failed."
  [url]
  (try (-> (http/get url  {:insecure? true})
           :body)
       (catch Exception e
         (timbre/warn (str (.getMessage e) " URL: " url)))))

(defn- parse-meta
  "Parse HTML content's metadata."
  [html property]
  (-> html
      (java.io.StringReader.)
      (html/html-resource)
      (html/select [[:meta (html/attr= :property property)]])
      (first)
      (get-in [:attrs :content])))

(defn- parse-description
  "Parse feed's og:description info, set :description to nil if not exist."
  [info]
  (let [html (or (fetch-html (:link info)) "")
        description (parse-meta html "og:description")]
    ;; (merge info {:description description})
    {:title       (str/trim (or (:title info) ""))
     :link        (str/trim (or (:link  info) ""))
     :description (str/trim (or description   ""))}))

(defn- parse-feed
  "Parse feed url to [{:link :title :description}] array."
  [url]
  (let [feed (feedparser/parse-feed url)
        info (map #(select-keys % [:title :link]) (:entries feed))]
    (map parse-description info)))

(defn- fetch-feed
  "Fetch feeds we need and send to channel."
  [feed-url]
  (go (doseq [f (parse-feed feed-url)]
        (>! channel f)
        (<! (timeout 500))                ; delay 500ms
        )))

(defn- fetch-planet-clojure
  "Fetch feeds from http://planet.clojure.in"
  []
  (fetch-feed "http://planet.clojure.in/atom.xml"))

(defn- fetch-mailing-list
  "Fetch clojure mailing-list from google-group."
  []
  (go (doseq [f (->> (parse-feed "https://groups.google.com/forum/feed/clojure/msgs/rss_v2_0.xml")
                     (filter #(or (re-matches #"\[ANN\].*"  (:title %))
                                  (re-matches #"\[ANNs\].*" (:title %))
                                  (re-matches #"ANN:.*"     (:title %)))))]
        (>! channel f)
        (<! (timeout 500))                ; delay 500ms
        )))

(defn- fetch-clojuretw-weekly
  "Fetch ClojureTW Weekly News. https://clojure.tw/weekly"
  []
  (fetch-feed "https://clojure.tw/weekly/feed.xml"))

(defn- fetch-subreddit-clojure
  "Fetch Clojure subreddit. https://www.reddit.com/r/Clojure"
  []
  (go (doseq [f (->> (parse-feed "https://www.reddit.com/r/Clojure.rss")
                     (map (fn [item] 
                            (update-in item [:link]
                                       (fn [url] (str "https://redd.it/" (get (str/split url #"\/") 6)))))))]
        (>! channel f)
        (<! (timeout 500))                ; delay 500ms
        )))

(defn clojuretw-weekly?
  "Check if url is part of https://clojure.tw/weekly"
  [url]
  (str/starts-with? url "https://clojure.tw/weekly"))

;; Async dispatcher
(go-loop []
  (let [{:keys [title link description] :as ch} (<! channel)]
    ;; check if this link is already store in db, if not push to telegram
    ;; if link is not valid, ignore it.
    (when (and (not (db/contains-link? link))
               (valid-link? link))
      (if (clojuretw-weekly? link)
        ;; special case for `https://clojure.tw/weekly` , we add instant view support
        (telegram/send-message! (str "https://t.me/iv?url=" link "&rhash=1ec8a497c1a0b2"))
        ;; some link may not be previewed by telegram
        (telegram/send-message! (if (empty? description) 
                                  (str "<b>" title "</b>\n" link) {:disable_web_page_preview true}
                                  (str link))))
      ;; Add link to db
      (db/add-link link)
      ;; To prevent get `HTTP response 429` from telegram, we need to limit message sending speed
      (<! (timeout 500))                ; delay 500ms
      ))
  (recur))

(defn fetch-all
  "Fetch all feeds we need and send to channel."
  []
  ;; planet clojure
  (fetch-planet-clojure)
  ;; Clojure mailing-lits
  (fetch-mailing-list)
  ;; ClojureTW weekly news
  (fetch-clojuretw-weekly)
  ;; Clojure subreddit
  (fetch-subreddit-clojure))
