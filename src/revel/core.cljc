(ns revel.core
  (:require [revel.geom :as g]
            [revel.layer :as layer]))

(defmethod layer/layer :default
  [_ plot & args]
  {:type :default})

(defmethod layer/layer ::point
  [_ plot & args]
  {:type ::point})

(defmethod layer/layer ::bar
  [_ plot & args]
  {:type ::bar})

(defn make-layer
  [plot type & args]
  (apply layer/layer type plot args))

(defrecord Plot [opts data])

(defn plot
  [opts & [data]]
  (->Plot opts data))

(defn layer
  [plot & args]
  (update plot :layers conj (apply make-layer plot args)))
