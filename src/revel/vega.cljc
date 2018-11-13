(ns revel.vega)

(defn vega
  [{:keys [opts data layers] :as plot} {:keys [width height]}]
  (assoc {}
         :width width
         :height height
         :padding 5
         :data [{:name "source" :values data}]
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
                    :type "linear",
                    :round true,
                    :nice true,
                    :zero true,
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
                  {:name "marks"
                   :type "symbol"
                   :from {:data "source"}
                   :encode
                   {:update
                    (reduce
                     (fn [acc [scale field]]
                       (assoc scale {:scale scale :field field}))
                     {:shape {:value "circle"},
                      :strokeWidth {:value 2},
                      :opacity {:value 0.5},
                      :stroke {:value "#4682b4"},
                      :fill {:value "transparent"}}
                     opts)}})))
