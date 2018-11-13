(ns revel.core
  (:require [revel.geom :as g]
            [revel.layer :as layer]))

(defmethod layer/layer :default
  [_ plot & args]
  {:layer :default})

(defmethod layer/layer ::point
  [_ plot & args]
  {:layer :point})

(defn make-layer
  [plot type & args]
  (apply layer type plot args))

(defrecord Plot [opts data])

(defn plot
  [opts & [data]]
  (->Plot opts data))

(defn layer
  [plot & args]
  (update plot :layers conj (apply make-layer plot args)))
