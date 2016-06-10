(ns ashes-hashes.endpoint.game
  (:require [compojure.core :refer :all]
            [net.cgrand.enlive-html :as html]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.rest.response :as esrsp]
            [clojurewerkz.elastisch.query :as q]
            [ashes-hashes.logsumer :as lg]))

(def facets
  (array-map
   :canonicalised_race "Species"
   :class "Background"
   :god "God"
   :killer "Killer"
   :canonicalised_version "Game Version"))

(defn game-to-str [doc]
  (str (:player_name doc) " the " (:charabbrev doc) " got " (:score doc)))

(def the-template "ashes_hashes/endpoint/game.html")

; Structure of the aggregations
(comment
  {:race
   {:doc_count_error_upper_bound 0
    :sum_other_doc_count 0
    :buckets
    [{:key "elf", :doc_count 1}]}})

(defn facet-value-default [facet refinement]
  (cond
    (and (= "god" facet) (empty? refinement)) "No God"
    (and (= "killer" facet) (empty? refinement)) "not killed"
    :else (str refinement)))

(html/defsnippet facet-refinement the-template [:dl :> #{:dt :dd}]
  [facet refinement]
  [:a]    (html/do->
           (html/content (facet-value-default facet (:key refinement)))
           (html/set-attr :href (str "/?" facet "=" (:key refinement))))
  [:span] (html/content (str (:doc_count refinement))))

(html/defsnippet facet-item the-template [:.facet]
  [[facet refinements]]
  [:h4] (html/content (clojure.string/capitalize (get facets facet)))
  [:dl] (html/content (map (partial facet-refinement (name facet)) (:buckets refinements))))

(html/defsnippet game-entry the-template  [:.game]
  [game]
  [:.game-score]  (html/content (str (:score game)))
  [:.game-player] (html/content (:player_name game))
  [:.game-combo]  (html/content (str (:race game) " " (:class game)))
  [:.game-ending] (html/content (:terse_msg game)))

(html/deftemplate games the-template
  [results]
  [:#games] (let [hits (esrsp/hits-from results)
                 games (map :_source hits)]
             (html/content (map game-entry games)))
  [:.facet] (let [res (esrsp/aggregations-from results)
                  facet-results (select-keys res (keys facets))]
            (html/content (map facet-item facet-results))))

(defn generate-query [params]
  (let [refinements (map #(q/terms % (get params %))
                         (filter lg/valid-game-key (keys params)))]
   (if (empty? refinements)
     (q/match-all)
     (first refinements))))

(defn show-game [config params]
  (let [results (esd/search (:conn (:es config)) lg/index-name lg/type-name
                            :query (generate-query params)
                            :aggs (into {} (map #(hash-map % {:terms {:field %}}) (keys facets))))
        html-page (games results)]
    html-page))

(defn game-endpoint [config]
  (routes
   (GET "/" {params :params} (show-game config params))))
