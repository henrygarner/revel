(ns revel.layer)

(defmulti layer
  (fn [type plot & args]
    type))
