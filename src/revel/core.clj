(ns revel.core
  (:require [revel.geom :as g]))

(defrecord Plot [data opts])

(defn plot
  [data opts]
  (->Plot data opts))

(def data
  (map #(hash-map :x % :y (+ % -10 (rand-int 20))) (range 0 200 2)))

(-> (plot data {})
    (g/point))


