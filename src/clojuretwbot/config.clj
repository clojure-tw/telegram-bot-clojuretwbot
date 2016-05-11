(ns clojuretwbot.config
  (:require [mount.core :refer [defstate]]
            [coldnew.config :as conf]))

(defstate env
  ;; Make coldnew.config evaluate the config after load it
  :start (conf/enable-eval!))
