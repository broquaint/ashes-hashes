(ns ashes-hashes.endpoint.game
  (:require [compojure.core :refer :all]
            [hiccup.core :as h]
            [clojurewerkz.elastisch.rest          :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]))

; (def welcome-page
;  (io/resource "ashes_hashes/endpoint/game/welcome.html"))

(defn show-game []
  (let [conn (esr/connect "http://127.0.0.1:9200")
        result (esd/get conn "scratch" "game" "AVDh6aR1nw9nrGaSIT86")
        doc (:_source result)]
    (h/html
     [:h1 (:character doc) " the " (:background doc) " got " (:score doc)])))

(defn game-endpoint [config]
  (routes
   (GET "/" [] (show-game))))
