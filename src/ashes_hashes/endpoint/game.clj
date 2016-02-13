(ns ashes-hashes.endpoint.game
  (:require [compojure.core :refer :all]
            [hiccup.core :as h]
            [net.cgrand.enlive-html :as html]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.rest.response :as esrsp]))

(defn game-to-str [doc]
  (str (:player_name doc) " the " (:charabbrev doc) " got " (:score doc)))

(def the-template "ashes_hashes/endpoint/game.html")

(html/defsnippet game-entry the-template  [:.game]
  [game]
  [:li] (html/content (game-to-str game)))

(html/deftemplate games the-template
  [games]
  [:.game] (html/content (map game-entry games)))

(defn show-game [config]
  (let [result (esd/search (:conn (:es config)) "scratch" "game")
        hits (esrsp/hits-from result)]
    (games (map :_source hits))))

(defn game-endpoint [config]
  (routes
   (GET "/" [] (show-game config))))
