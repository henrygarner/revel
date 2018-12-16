(ns revel.core
  (:require [revel.geom :as g]
            [revel.data :as d]
            [kixi.stats.core :as kixi]
            [kixi.stats.estimate :as estimate]
            [kixi.stats.math :refer [sq sqrt]]
            [kixi.stats.protocols :as p]))

(defn bin
  "Ignores opts to sort & bin data into 20 equally-sized bins"
  [f data opts]
  (let [n-bins 50
        xs (map f data)
        min-x (apply min xs)
        max-x (apply max xs)
        range-x (- max-x min-x)
        max-bin (dec n-bins)
        bin-num (fn [x]
                  (-> x
                      (- min-x)
                      (/ range-x)
                      (* n-bins)
                      (int)
                      (min max-bin)))
        bin-width (/ range-x n-bins)
        bin-bounds (fn [bin]
                     (let [lower (-> (/ bin n-bins)
                                     (* range-x)
                                     (+ min-x))]
                       [lower (+ lower bin-width)]))]
    (->> (group-by (comp bin-num f) data)
         (map (fn [[k v]]
                (with-meta v {:bin k
                              :bounds (bin-bounds k)}))))))

(defmulti layer-domain
  (fn [{:keys [type]} data]
    type))

(defmethod layer-domain :default
  [{:keys [x y]} data]
  (let [records (d/to-records data)
        xs (map x records)
        ys (map y records)
        min-x (apply min xs)
        max-x (apply max xs)
        min-y (apply min ys)
        max-y (apply max ys)]
    ;; Should be domain for all scales (not just axes)
    {:data data
     :domain {:x [min-x max-x]
              :y [min-y max-y]}}))

(defmethod layer-domain ::histogram
  [{:keys [x]} data]
  (let [binned (bin x (d/to-records data) {})
        records (map (fn [xs]
                       (let [bin (-> xs meta :bin)
                             [lower upper] (-> xs meta :bounds)]
                         {:bin bin
                          :lower-bound (float lower)
                          :upper-bound (float upper)
                          :count (count xs)}))
                     binned)]
    {:data (d/from-records records)
     :domain {:x [(apply min (map :lower-bound records))
                  (apply max (map :upper-bound records))]
              :y [0 (apply max (map :count records))]}}))

(defmethod layer-domain ::linear-model
  [{:keys [x y]} data]
  (let [records (d/to-records data)
        xs (map x records)
        ys (map y records)
        min-x (apply min xs)
        max-x (apply max xs)
        range-x (- max-x min-x)
        min-y (apply min ys)
        max-y (apply max ys)
        sum-squares (transduce identity (kixi/sum-squares x y) records)
        regression (estimate/simple-linear-regression sum-squares)
        alpha 0.05
        ci #(estimate/regression-confidence-interval sum-squares % alpha)
        pi #(estimate/regression-prediction-interval sum-squares % alpha)]
    {:data (d/from-records (map #(let [cix (ci %)
                                       pix (pi %)]
                                   (hash-map :x %
                                             :y (p/measure regression %)
                                             :estimate-upper (p/upper cix)
                                             :estimate-lower (p/lower cix)
                                             :prediction-upper (p/upper pix)
                                             :prediction-lower (p/lower pix)))
                                (range min-x max-x (/ range-x 100.0))))
     :domain {:x [min-x max-x]
              :y [min-y max-y]}}))

(defn assoc-default
  [coll k v]
  (cond-> coll
    (nil? (get coll k))
    (assoc k v)))

(defn calculate-domains
  "Takes configuration and data, returning a representation primed with domains from the data"
  [{layers :layers opts :opts :as plot} data]
  (let [domain-layers (map #(merge {:opts opts}
                                   (layer-domain (merge opts %) data)
                                   %)
                           layers)]
    (assoc plot :layers domain-layers)))

(defmulti make-layer
  (fn [type plot & args]
    type))

(defmethod make-layer :default
  [type plot & args]
  {:type type})

(defmethod make-layer ::point
  [_ plot & args]
  {:type ::point})

(defmethod make-layer ::bar
  [_ plot & args]
  {:type ::bar})

(defmethod make-layer ::histogram
  [_ plot & args]
  {:type ::histogram})

(defmethod make-layer ::heatmap
  [_ plot & args]
  {:type ::heatmap})

(defrecord Plot [opts data])

(defn plot
  [opts & [data]]
  (->Plot opts data))

(defn layer
  [plot type & args]
  (update plot :layers conj (apply make-layer type plot args)))

(defn facet
  [plot on & args]
  (assoc plot :facet {:on on :args args}))

(defmulti render
  (fn [plot type & args]
    type))

(defmethod render :default
  [plot _ & args]
  "Unknown render function")

(defn data
  [plot data]
  (dissoc (calculate-domains plot data) :data :opts))
