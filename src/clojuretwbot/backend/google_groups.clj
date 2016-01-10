(ns clojuretwbot.backend.google-groups
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.core.async :refer [chan go go-loop >! <! timeout alt! put! <!!] :as async]))

(defonce channel (chan))

(def ^:private API_URL
  "https://query.yahooapis.com/v1/public/yql")

(defn build-query [url]
  (str "select * from rss where url='" url "'"))

(defn fetch-rss [url]
  "Fetch RSS result based on YQL."
  (http/get API_URL
            {:query-params {:q (build-query url)
                            :nums 50
                            :format "json"}}))

(defn parse-rss
  "Get all rss content in vector, every feed store in map format."
  [url]
  (-> (fetch-rss url)
      :body
      (json/read-str :key-fn keyword)
      :query
      :results
      :item))

(defn find-ANN
  "Find ANN content."
  [url]
  (doseq [k (filter #(or (re-matches #"\[ANN\].*" (:title %))
                         (re-matches #"ANN:.*" (:title %)))
                    (parse-rss url))]
    ;; push to channel after init
    (put! channel k)))

(defn find-ANN-in-clojure-list
  "Find ANN contents in clojure mailing-list."
  ([_ _] (find-ANN-in-clojure-list))
  ([]
   (find-ANN "https://groups.google.com/forum/feed/clojure/msgs/rss_v2_0.xml")))
