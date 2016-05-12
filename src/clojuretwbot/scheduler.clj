(ns clojuretwbot.scheduler
  (:require [clojuretwbot.scheduler.feed-poster :as feed]
            [hara.io.scheduler :as sch]
            [taoensso.timbre :as timbre]))

(def scheduler
  (sch/scheduler
   ;; every 30 min send feeds link
   {:feed-fetcher {:handler (fn [_ _] (feed/fetch-all))
                   :schedule "0 /30 * * * * *"
                   :params nil}
    }))

(defn start
  "Start scheduler"
  []
  (sch/start! scheduler)
  (timbre/info "start scheduler for clojuretwbot."))

(defn stop
  "Stop scheduler"
  []
  (sch/stop! scheduler)
  (timbre/info "stop scheduler for clojuretwbot."))