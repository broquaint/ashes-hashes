(ns ashes-hashes.endpoint.game
  (:require [compojure.core :refer :all]
            [hiccup.core :as h]
            [net.cgrand.enlive-html :as html]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.rest.response :as esrsp]
            [clojurewerkz.elastisch.query :as q]))

(defn game-to-str [doc]
  (str (:player_name doc) " the " (:charabbrev doc) " got " (:score doc)))

(def the-template "ashes_hashes/endpoint/game.html")

(html/defsnippet facet-refinement the-template [:.facet :li]
  [refinement]
  [:a]    (html/content (str (:key  refinement)))
  [:span] (html/content (str (:doc_count refinement))))

(html/defsnippet facet-item the-template [:.facet]
  [[name facet]]
  [:h4] (html/content (str name))
  [:li] (html/content (map facet-refinement (:buckets facet))))

(html/defsnippet game-entry the-template  [:.game]
  [game]
  [:li] (html/content (game-to-str game)))

; Structure of the aggregations
(comment
  {:races
   {:doc_count_error_upper_bound 0,
    :sum_other_doc_count 0,
    :buckets
    [{:key "elf", :doc_count 1}]}})

(html/deftemplate games the-template
  [results]
  [:.game] (let [hits (esrsp/hits-from results)
                 games (map :_source hits)]
             (html/content (map game-entry games)))
  [:.facet] (let [facets (esrsp/aggregations-from results)]
            (html/content (map facet-item facets))))

(defn show-game [config]
  (let [results (esd/search (:conn (:es config)) "scratch" "game"
                            :query (q/match-all)
                            :aggs {:titles {:terms {:field :title}}
                                   :races  {:terms {:field :race}}})]
    (games results)))

(defn game-endpoint [config]
  (routes
   (GET "/" [] (show-game config))))
