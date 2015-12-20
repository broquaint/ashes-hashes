(ns ashes-hashes.system
  (:require [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [duct.component.endpoint :refer [endpoint-component]]
            [duct.component.handler :refer [handler-component]]
            [duct.component.hikaricp :refer [hikaricp]]
            [duct.middleware.not-found :refer [wrap-not-found]]
            [duct.middleware.route-aliases :refer [wrap-route-aliases]]
            [meta-merge.core :refer [meta-merge]]
            [ring.component.jetty :refer [jetty-server]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.webjars :refer [wrap-webjars]]
            [ashes-hashes.endpoint.game :refer [game-endpoint]]
            [ashes-hashes.component.elastich :refer [elastich-component]]))

(def base-config
  {:app {:middleware [[wrap-not-found :not-found]
                      [wrap-webjars]
                      [wrap-defaults :defaults]
                      [wrap-route-aliases :aliases]]
         :not-found  (io/resource "ashes_hashes/errors/404.html")
         :defaults   (meta-merge site-defaults {:static {:resources "ashes_hashes/public"}})
         :aliases    {}
         }})

(defn new-system [config]
  (let [config (meta-merge base-config config)]
    (-> (component/system-map
         :app  (handler-component (:app config))
         :http (jetty-server (:http config))
         :game (endpoint-component game-endpoint)
         :db   (hikaricp (:db config))
         :es   (elastich-component (:es config)))
        (component/system-using
         {:http [:app]
          :app  [:game]
          :db   []
          :game [:es]}))))
