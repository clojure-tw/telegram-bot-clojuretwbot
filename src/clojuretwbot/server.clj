(ns clojuretwbot.server
  (:require [compojure.core :refer [routes defroutes wrap-routes]]
            [compojure.route :as route :refer [resources]]
            [compojure.handler :as handler]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [prone.middleware :refer [wrap-exceptions]]
            [coldnew.config :refer [conf]]
            [ring.adapter.jetty :refer [run-jetty]]
            [taoensso.timbre :as timbre]
            [clojuretwbot.routes.api  :refer [api-routes]]
            [clojuretwbot.routes.site :refer [site-routes]])
  (:gen-class))

(def enable-debug?
  (= "true" (conf :enable-debug)))

(defroutes not-found
  (route/not-found "404"))

(defroutes app-routes
  site-routes
  api-routes
  not-found)

(defroutes http-handler
  (-> (handler/site app-routes)
      wrap-with-logger
      wrap-gzip
      wrap-exceptions
      (cond-> enable-debug? wrap-exceptions)))

(defn start [& [port]]
  (let [port (Integer. (or port (conf :port) 8080))
        host (or (System/getenv "OPENSHIFT_DIY_IP") "127.0.0.1")]
    (timbre/info "start ring-handler on port " port)
    (run-jetty http-handler {:host host :port port})))

(defn stop [this]
  (.stop this))