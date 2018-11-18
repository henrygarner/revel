(ns revel.data)

(derive ::factor ::variable)

(derive ::continuous ::variable)

(defprotocol IFrame
  (factor? [this column])
  (col-type [this column])
  (to-records [this]))

(defn from-records
  [records]
  (let [f (first records)]
    (reify IFrame
      (factor? [_ column] (string? (get f column)))
      (col-type [this column]
        (if (factor? this column)
          ::factor
          ::continuous))
      (to-records [_] records))))

(defn frame
  [data]
  data)
