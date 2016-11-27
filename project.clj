(defproject clojuretwbot "0.2.0"
  :description "A telegram bot for clojure.tw channel."
  :url "https://github.com/clojure-tw/telegram-bot-clojuretwbot"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.395"]
                 [clj-http "3.4.1"]
                 [cheshire "5.6.3"]
                 [org.clojars.scsibug/feedparser-clj "0.4.0"]
                 [org.clojure/data.json "0.2.6"]
                 [com.cemerick/url "0.1.1"]
                 [com.taoensso/timbre "4.7.4"]
                 [yesql "0.5.3"]
                 [org.xerial/sqlite-jdbc "3.15.1"]
                 [migratus "0.8.32"]
                 [im.chit/hara.io.scheduler "2.4.8"]
                 [im.chit/hara.time.joda "2.2.17"]
                 [enlive "1.1.6"]
                 [ring "1.5.0"]
                 [ring/ring-defaults "0.2.1"]
                 [bk/ring-gzip "0.1.1"]
                 [ring.middleware.logger "0.5.0"]
                 [compojure "1.5.1"]
                 [metosin/compojure-api "1.1.9"]
                 [prismatic/schema "1.1.3"]
                 [metosin/ring-http-response "0.8.0"]
                 [mount "0.1.11-SNAPSHOT"]
                 [coldnew/config "1.2.0"]
                 [prone "1.1.4"]]

  :plugins [[migratus-lein "0.2.6"]]

  :jvm-opts ["-Dclojure.compiler.direct-linking=true"]

  :migratus {:store :database}

  :main ^:skip-aot clojuretwbot.core)
