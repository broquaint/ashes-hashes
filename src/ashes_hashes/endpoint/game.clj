(ns ashes-hashes.endpoint.game
  (:require [compojure.core :refer :all]
            [net.cgrand.enlive-html :as html]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.rest.response :as esrsp]
            [clojurewerkz.elastisch.query :as q]))

(def facets
  {:race "Species"
   :class "Background"
   :god "God"
   :terse_msg "Final Message"
   :version "Game Version"})

(defn game-to-str [doc]
  (str (:player_name doc) " the " (:charabbrev doc) " got " (:score doc)))

(def the-template "ashes_hashes/endpoint/game.html")

(html/defsnippet facet-refinement the-template [:.facet :li]
  [facet refinement]
  [:a]    (html/do->
           (html/content (str (:key refinement)))
           (html/set-attr :href (str "/?" facet "=" (:key refinement))))
  [:span] (html/content (str (:doc_count refinement))))

(html/defsnippet facet-item the-template [:.facet]
  [[facet refinements]]
  [:h4] (html/content (clojure.string/capitalize (get facets facet)))
  [:ul] (html/content (map (partial facet-refinement (name facet)) (:buckets refinements))))

(html/defsnippet game-entry the-template  [:.game]
  [game]
  [:li] (html/content (game-to-str game)))

; Structure of the aggregations
(comment
  {:race
   {:doc_count_error_upper_bound 0
    :sum_other_doc_count 0
    :buckets
    [{:key "elf", :doc_count 1}]}})

(html/deftemplate games the-template
  [results]
  [:#games] (let [hits (esrsp/hits-from results)
                 games (map :_source hits)]
             (html/content (map game-entry games)))
  [:.facet] (let [facets (esrsp/aggregations-from results)]
            (html/content (map facet-item facets))))

(defn generate-query [params]
  (let [refinements (map #(q/terms % (get params %))
                         (filter (partial contains? params) (keys facets)))]
   (if (empty? refinements)
     (q/match-all)
     (first refinements))))

(defn show-game [config params]
  (let [results (esd/search (:conn (:es config)) "scratch" "game"
                            :query (generate-query params)
                            :aggs (into {} (map #(hash-map % {:terms {:field %}}) (keys facets))))
        html-page (games results)]
    html-page))

(defn game-endpoint [config]
  (routes
   (GET "/" {params :params} (show-game config params))))
