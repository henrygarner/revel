(ns revel.data)

(defprotocol IFrame
  (factor? [this column])
  (to-records [this]))

(defn from-records
  [records]
  (let [f (first records)]
    (reify IFrame
      (factor? [_ column] (string? (get f column)))
      (to-records [_] records))))

(defn frame
  [data]
  data)
