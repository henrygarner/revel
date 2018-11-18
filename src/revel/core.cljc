(ns revel.core
  (:require [revel.geom :as g]))

(defmulti make-layer
  (fn [type plot & args]
    type))

(defmethod make-layer :default
  [_ plot & args]
  {:type :default})

(defmethod make-layer ::point
  [_ plot & args]
  {:type ::point})

(defmethod make-layer ::bar
  [_ plot & args]
  {:type ::bar})

(defrecord Plot [opts data])

(defn plot
  [opts & [data]]
  (->Plot opts data))

(defn layer
  [plot type & args]
  (update plot :layers conj (apply make-layer type plot args)))

(defmulti render
  (fn [plot type & args]
    type))

(defmethod render :default
  [plot _ & args]
  "Unknown render function")
