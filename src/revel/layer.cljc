(ns revel.layer)

(defmulti layer
  (fn [type plot & args]
    type))

(defmethod layer :default
  [_ plot & args]
  {:type ::default})
