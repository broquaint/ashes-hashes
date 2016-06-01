(ns ashes-hashes.component.elastich
  (:require [clojurewerkz.elastisch.rest :as esr]
            [com.stuartsierra.component :as component]))

(defrecord ElasticSearchComponent [uri]
  component/Lifecycle
  (start [component]
    (let [uri (:uri component)]
      (if (nil? uri)
        (throw (IllegalArgumentException. "Didn't get a URI for Elasticsearch!")))
      (if (:conn component)
        component
        (assoc component :conn (esr/connect (:uri component))))))
  (stop [component]
    (dissoc component :conn)))

(defn elastich-component [options]
  (map->ElasticSearchComponent options))

