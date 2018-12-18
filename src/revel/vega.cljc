(ns revel.vega
  (:require [revel.core :as r]
            [revel.data :as d :refer [col-type to-records factor?]]))

(defmulti to-mark
  (fn [{:keys [type] :as layer} {:keys [opts data] :as plot} args]
    [type (some->> (get opts :x) (col-type data)) (some->> (get opts :y) (col-type data))]))

(defmethod to-mark
  [::r/point ::d/variable ::d/variable]
  [_ {:keys [opts] :as plot} _]
  {:name "marks"
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
     opts)}})

(defmethod to-mark
  [::r/bar ::d/factor ::d/continuous]
  [_ {:keys [opts] :as plot} _]
  {:type "rect"
   :from {:data "source"}
   :encode {:enter (reduce
                    (fn [acc [scale field]]
                      (case scale
                        :x (-> acc
                               (assoc :x {:scale scale :field field})
                               (assoc :width {:scale scale :band 1}))
                        :y (-> acc
                               (assoc :y {:scale scale :field field})
                               (assoc :y2 {:scale scale :value 0}))))
                                               
                    {}
                    (select-keys opts [:x :y]))}})

(defmethod to-mark
  [::r/bar ::d/continuous ::d/factor]
  [_ {:keys [opts] :as plot} _]
  {:type "rect"
   :from {:data "source"}
   :encode {:enter (reduce
                    (fn [acc [scale field]]
                      (case scale
                        :x (-> acc
                               (assoc :x {:scale scale :value 0})
                               (assoc :width {:scale scale :field field}))
                        :y (-> acc
                               (assoc :y {:scale scale :field field})
                               (assoc :height {:scale scale :band 1}))))
                                               
                    {}
                    (select-keys opts [:x :y]))}})

(defmethod to-mark [::r/histogram ::d/variable nil]
  [_ {:keys [opts] :as plot} _]
  ;; We need to access pre-binned data (or to update 'data' specification ourselves)
  ;; If the former, by what name will our data be available?
  )

(defmulti add-layer
  (fn [vega {:keys [type opts data] :as layer}]
    [type (some->> (get opts :x) (col-type data)) (some->> (get opts :y) (col-type data))]))

(def layer-id (atom 0))

(defmethod add-layer
  [::r/point ::d/variable ::d/variable]
  [vega {:keys [opts data domain] :as layer}]
  (let [source-name (str "source-" (swap! layer-id inc))]
    (-> vega
        (update :data conj {:name source-name :values (to-records data)})
        (update :marks conj {:name "marks"
                             :type "symbol"
                             :from {:data source-name}
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
                               opts)}})
        (update :axes concat (for [{:keys [scale title orient]}
                                   [{:scale :x
                                     :title (get opts :x)
                                     :orient "bottom"}
                                    {:scale :y
                                     :title (get opts :y)
                                     :orient "left"}]
                                   :when title]
                               {:scale scale,
                                :grid true
                                :domain (get domain scale)
                                :orient orient
                                :tickCount 5,
                                :title title}))
        (update :scales concat (for [[scale field] opts]
                                 {:name scale,
                                  :type (if (factor? data field)
                                          "band"
                                          "linear"),
                                  :round true,
                                  :nice true,
                                  :zero false,
                                  :padding (if (factor? data field) 0.05 0)
                                  :domain (get domain scale)
                                  :range (case scale
                                           :x :width
                                           :y :height
                                           :size [4 361])}))
        #_(update :legends concat (for [[scale field] (dissoc opts :x :y)]
                                  {:size scale,
                                   :title field,
                                   :format "s",
                                   :encode
                                   {:symbols
                                    {:update
                                     {:strokeWidth {:value 2},
                                      :opacity {:value 0.5},
                                      :stroke {:value "#4682b4"},
                                      :shape {:value "circle"}}}}})))))

(defmethod add-layer
  [::r/histogram ::d/variable nil]
  [vega {:keys [opts data domain] :as layer}]
  (let [source-name (str "source-" (swap! layer-id inc))]
    (-> vega
        (update :data conj {:name source-name :values (to-records data)})
        (update :marks conj {:type "rect"
                             :from {:data source-name}
                             :encode {:update {:x {:scale :x :field :lower-bound}
                                               :x2 {:scale :x :field :upper-bound}
                                               :y {:scale :y :field :count}
                                               :y2 {:scale :y :value 0}
                                               :fill {:value "steelblue"}}}})
        (update :axes concat [{:scale :x
                               :domain (get domain :x)
                               :orient "bottom"
                               :title "x"}
                              {:scale :y
                               :domain (get domain :y)
                               :orient "left"
                               :title "count"}])
        (update :scales concat [{:name :x,
                                 :type "linear",
                                 :domain (get domain :x)
                                 :zero false
                                 :range :width}
                                {:name :y
                                 :type "linear"
                                 :domain (get domain :y)
                                 :zero false
                                 :range :height}]))))

(defmethod add-layer
  [::r/linear-model ::d/variable ::d/variable]
  [vega {:keys [opts data domain] :as layer}]
  (let [source-name (str "source-" (swap! layer-id inc))]
    (-> vega
        (update :data conj {:name source-name :values (to-records data)})
        (update :marks concat [{:type "line"
                                :clip true
                                :from {:data source-name}
                                :encode {:update {:x {:scale :x :field :x}
                                                  :y {:scale :y :field :y}
                                                  :stroke {:value "blue"}
                                                  :strokeWidth {:value 2}}}}
                               {:type "area"
                                :clip true
                                :from {:data source-name}
                                :encode {:update {:interpolate {:value "monotone"}
                                                  :x {:scale :x :field :x}
                                                  :y {:scale :y :field :prediction-lower}
                                                  :y2 {:scale :y :field :prediction-upper}
                                                  :fill {:value "gray"}
                                                  :fillOpacity {:value 0.1}}}}
                               {:type "area"
                                :clip true
                                :from {:data source-name}
                                :encode {:update {:interpolate {:value "monotone"}
                                                  :x {:scale :x :field :x}
                                                  :y {:scale :y :field :estimate-lower}
                                                  :y2 {:scale :y :field :estimate-upper}
                                                  :fill {:value "gray"}
                                                  :fillOpacity {:value 0.25}}}}]))))

(defn vega*
  [{:keys [layers opts domain data] :as plot} {:keys [width height] :as args}]
  (reduce (fn [vega layer]
            (add-layer vega layer))
          (assoc {}
                 :width width
                 :height height
                 :padding 5
                 :data []
                 :axes (vec (for [{:keys [scale title orient]}
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
                 :scales [] #_(for [[scale field] opts]
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
                 :legends [] #_(for [[scale field] (dissoc opts :x :y)]
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
                 :marks [] #_(for [layer layers]
                               (to-mark layer plot opts)))
          (reverse layers)))

(defmethod r/render ::vega
  [plot _ & args]
  (apply vega* plot args))
