(ns ashes-hashes.endpoint.example
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]))

(def welcome-page
  (io/resource "ashes_hashes/endpoint/example/welcome.html"))

(defn example-endpoint [config]
  (routes
   (GET "/" [] welcome-page)))
