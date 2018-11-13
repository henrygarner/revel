(ns revel.core
  (:require [revel.geom :as g]))

(defrecord Plot [data opts])

(defn plot
  [data opts]
  (->Plot data opts))


