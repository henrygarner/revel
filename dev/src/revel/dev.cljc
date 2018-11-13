(ns revel.dev
  (:require [clojure.java.io :as io]
            [cheshire.core :refer [parse-string]]
            [revel.core :as r]
            [revel.vega :as v]
            [revel.data :as d]
            [oz.core :as oz]))

#_(oz/start-plot-server!)

(def cars-data
  (parse-string (slurp (io/resource "cars.json"))))

(-> (r/plot (d/frame cars-data)))

(defn group-data [& names]
  (apply concat
         (for [n names]
           (map-indexed (fn [i x] {:x i :y x :col n}) (take 20 (repeatedly #(rand-int 100)))))))

(def line-plot
  {:data {:values (group-data "monkey" "slipper" "broom")}
   :encoding {:x {:field "x"}
              :y {:field "y"}
              :color {:field "col" :type "nominal"}}
   :mark "line"})

(def cars-plot
  {:legends
   [{:size "size",
     :title "Acceleration",
     :format "s",
     :encode
     {:symbols
      {:update
       {:strokeWidth {:value 2},
        :opacity {:value 0.5},
        :stroke {:value "#4682b4"},
        :shape {:value "circle"}}}}}],
   :axes
   [{:scale "x",
     :grid true,
     :domain false,
     :orient "bottom",
     :tickCount 5,
     :title "Horsepower"}
    {:scale "y",
     :grid true,
     :domain false,
     :orient "left",
     :titlePadding 5,
     :title "Miles_per_Gallon"}],
   :width 200,
   :scales
   [{:name "x",
     :type "linear",
     :round true,
     :nice true,
     :zero true,
     :domain
     {:data "source", :field "Horsepower"},
     :range "width"}
    {:name "y",
     :type "linear",
     :round true,
     :nice true,
     :zero true,
     :domain
     {:data "source", :field "Miles_per_Gallon"},
     :range "height"}
    {:name "size",
     :type "linear",
     :round true,
     :nice false,
     :zero true,
     :domain
     {:data "source", :field "Acceleration"},
     :range [4 361]}],
   :padding 5,
   :marks
   [{:name "marks",
     :type "symbol",
     :from {:data "source"},
     :encode
     {:update
      {:x {:scale "x", :field "Horsepower"},
       :y
       {:scale "y", :field "Miles_per_Gallon"},
       :size
       {:scale "size", :field "Acceleration"},
       :shape {:value "circle"},
       :strokeWidth {:value 2},
       :opacity {:value 0.5},
       :stroke {:value "#4682b4"},
       :fill {:value "transparent"}}}}],
   :$schema
   "https://vega.github.io/schema/vega/v3.0.json",
   :height 200,
   :data
   [{:name "source",
     :url
     "https://vega.github.io/vega/data/cars.json",
     :transform
     [{:type "filter",
       :expr
       "datum['Horsepower'] != null && datum['Miles_per_Gallon'] != null && datum['Acceleration'] != null"}]}]})

;; Render the plot to the 
#_(oz/v! cars-plot :mode :vega)
