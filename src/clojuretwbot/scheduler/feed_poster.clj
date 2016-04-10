(ns clojuretwbot.scheduler.feed-poster
  (:require [clj-http.client :as http]
            [clojure.core.async :as async :refer [<! chan go-loop put!]]
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
         (timbre/warn (.getMessage e)))))

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
    (merge info {:description description})))

(defn- parse-feed
  "Parse feed url to [{:link :title :description}] array."
  [url]
  (let [feed (feedparser/parse-feed url)
        info (map #(select-keys % [:title :link]) (:entries feed))]
    (map parse-description info)))

(defn- fetch-feed
  "Fetch feeds we need and send to channel."
  [feed-url]
  (doseq [f (parse-feed feed-url)]
    (put! channel f)))

(defn- fetch-planet-clojure
  "Fetch feeds from http://planet.clojure.in"
  []
  (fetch-feed "http://planet.clojure.in/atom.xml"))

(defn- fetch-mailing-list
  "Fetch clojure mailing-list from google-group."
  []
  (doseq [f (->> (parse-feed "https://groups.google.com/forum/feed/clojure/msgs/rss_v2_0.xml")
                 (filter #(or (re-matches #"\[ANN\].*" (:title %))
                              (re-matches #"ANN:.*" (:title %)))))]
    (put! channel f)))

(defn- fetch-coldnew-blog
  "Fetch clojure/clojurescript post from http://coldnew.github.io/rss.xml."
  []
  (doseq [f (->> (parse-feed "http://coldnew.github.io/rss.xml")
                 (filter #(-> (fetch-html (:link %))
                              ((fn [x] (re-find #"(<meta\s*name=\"keywords\"\s*content=\")(.*)(\"\s*/>)")))
                              (nth 2)
                              (str/includes? "clojure"))))]
    (put! channel f)))

(defn- fetch-fnil-net
  "Fetch http://blog.fnil.net"
  []
  (doseq [f (->> (parse-feed "http://blog.fnil.net/atom.xml")
                 (filter #(-> (fetch-html (:link %))
                              ((fn [x] (re-find #"(<a\s*class='category'\s*href=.*'>)(.*)(</a>)" x)))
                              (nth 2)
                              (str/includes? "clojure"))))]
    (put! channel f)))

;; Async dispatcher
(go-loop []
  (let [{:keys [title link description] :as ch} (<! channel)]
    ;; check if this link is already store in db, if not push to telegram
    ;; if link is not valid, ignore it.
    (when (and (not (db/contains-link? link))
               (valid-link? link))
      (if (nil? description) ; some link can't be previewed by telegram
        (telegram/send-message! (str "<b>" title "</b>\n" link) {:disable_web_page_preview true})
        (telegram/send-message! (str link)))
      ;; Add link to db
      (db/add-link link)))
  (recur))

(defn fetch-all
  "Fetch all feeds we need and send to channel."
  []
  ;; planet clojure
  (fetch-planet-clojure)
  ;; Clojure mailing-lits
  (fetch-mailing-list)
  ;; coldnew's blog (chinese)
  (fetch-coldnew-blog)
  ;; 庄周梦蝶 (chinese)
  (fetch-fnil-net))
