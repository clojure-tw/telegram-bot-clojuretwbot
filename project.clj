(defproject wenziyu "0.1.0-SNAPSHOT"
  :description "A telegram bot to indicate long sentance"
  :url "https://github.com/clojure-tw/what-you-say-is-too-long"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.2.374"]
                 [clj-http "2.0.0"]
                 [cheshire "5.5.0"]
                 [org.clojars.scsibug/feedparser-clj "0.4.0"]
                 [org.clojure/data.json "0.2.6"]]

  :plugins [[michaelblume/lein-marginalia "0.9.0"]]
  :main ^:skip-aot wenziyu.core)
