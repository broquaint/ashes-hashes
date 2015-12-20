(ns ashes-hashes.endpoint.game
  (:require [compojure.core :refer :all]
            [hiccup.core :as h]
            [clojurewerkz.elastisch.rest.document :as esd]))

(defn show-game [config]
  (let [result (esd/get (:conn (:es config)) "scratch" "game" "AVDh6aR1nw9nrGaSIT86")
        doc (:_source result)]
    (h/html
     [:h1 (:character doc) " the " (:background doc) " got " (:score doc)]
     "\n")))

(defn game-endpoint [config]
  (routes
   (GET "/" [] (show-game config))))
