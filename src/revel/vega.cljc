(ns revel.vega
  (:require [revel.core :as r]
            [revel.data :refer [to-records factor?]]))

(defn vega*
  [{:keys [opts data layers] :as plot} {:keys [width height]}]
  (assoc {}
         :width width
         :height height
         :padding 5
         :data [{:name "source" :values (to-records data)}]
         :axes (for [{:keys [scale title orient]}
                     [{:scale :x
                       :title (get opts :x)
                       :orient "bottom"}
                      {:scale :y
                       :title (get opts :y)
                       :orient "left"}]
                     :when title]
                 {:scale scale,
                  :grid true
                  :domain false,
                  :orient orient
                  :tickCount 5,
                  :title title})
         :scales (for [[scale field] opts]
                   {:name scale,
                    :type (if (factor? data field)
                            "band"
                            "linear"),
                    :round true,
                    :nice true,
                    :zero true,
                    :padding (if (factor? data field) 0.05 0)
                    :domain
                    {:data "source", :field field},
                    :range (case scale
                             :x :width
                             :y :height
                             :size [4 361])})
         :legends (for [[scale field] (dissoc opts :x :y)]
                    {:size scale,
                     :title field,
                     :format "s",
                     :encode
                     {:symbols
                      {:update
                       {:strokeWidth {:value 2},
                        :opacity {:value 0.5},
                        :stroke {:value "#4682b4"},
                        :shape {:value "circle"}}}}})
         :marks (for [{:keys [type]} layers]
                  (do (println type)
                   (case type
                     ::r/point {:name "marks"
                                :type "symbol"
                                :from {:data "source"}
                                :encode
                                {:update
                                 (reduce
                                  (fn [acc [scale field]]
                                    (assoc acc scale {:scale scale :field field}))
                                  {:shape {:value "circle"},
                                   :strokeWidth {:value 2},
                                   :opacity {:value 0.5},
                                   :stroke {:value "#4682b4"},
                                   :fill {:value "transparent"}}
                                  opts)}}
                     ::r/bar {:type "rect"
                              :from {:data "source"}
                              :encode {:enter (reduce
                                               (fn [acc [scale field]]
                                                 (case scale
                                                   :x (-> acc
                                                          ;; (assoc :x {:scale scale :field field})
                                                          ;; (assoc :width {:scale scale :band 1})
                                                          (assoc :x {:scale scale :value 0})
                                                          (assoc :width {:scale scale :field field}))
                                                   :y (-> acc
                                                          ;; (assoc :y {:scale scale :field field})
                                                          ;; (assoc :y2 {:scale scale :value 0})
                                                          (assoc :y {:scale scale :field field})
                                                          (assoc :height {:scale scale :band 1}))))
                                               
                                               {}
                                               (select-keys opts [:x :y]))}})))))

(defmethod r/render ::vega
  [plot _ & args]
  (apply vega* plot args))
