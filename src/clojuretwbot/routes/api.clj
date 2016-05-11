(ns clojuretwbot.routes.api
  (:require [ring.util.http-response :as response]
            [ring.util.http-status :as status]
            [compojure.api.sweet :as sweet
             :refer [defapi context]]
            [compojure.api.core :refer [GET]]
            [schema.core :as s]
            [clojuretwbot.db :refer [aa]]
            ))

(defapi api-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Basic API"
                           :description "Basic Service"}}}}
  (context "/api" []
           :tags ["thingie"]


           (GET "/all" []
                :return String
                :summary "this is a test"
                (status/ok (aa)))


           ))