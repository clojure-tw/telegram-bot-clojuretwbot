(defproject clojuretwbot "0.2.0"
  :description "A telegram bot for clojure.tw channel."
  :url "https://github.com/clojure-tw/telegram-bot-clojuretwbot"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.374"]
                 [clj-http "2.1.0"]
                 [cheshire "5.5.0"]
                 [org.clojars.scsibug/feedparser-clj "0.4.0"]
                 [org.clojure/data.json "0.2.6"]
                 [com.cemerick/url "0.1.1"]
                 [com.taoensso/timbre "4.3.1"]
                 [yesql "0.5.2"]
                 [org.xerial/sqlite-jdbc "3.8.11.2"]
                 [migratus "0.8.13"]
                 [environ "1.0.2"]
                 [im.chit/hara.io.scheduler "2.2.17"]
                 [im.chit/hara.time.joda "2.2.17"]]

  :plugins [[migratus-lein "0.2.6"]]

  :jvm-opts ["-Dclojure.compiler.direct-linking=true"]

  :migratus {:store :database}

  :main ^:skip-aot clojuretwbot.core)
