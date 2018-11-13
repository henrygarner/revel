(ns revel.dev
  (:require [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [clojure.java.io :as io]
            [cheshire.core :refer [parse-string]]
            [revel.core :as r]
            [revel.vega :as v]
            [revel.data :as d]
            [oz.core :as oz]))

(oz/start-plot-server!)

(def cars-data
  (take 1000 (transform-keys ->kebab-case-keyword (parse-string (slurp (io/resource "cars.json"))))))


(-> (r/plot {:x :horsepower :y :miles-per-gallon :size :acceleration} (d/from-records cars-data))
    (r/layer ::r/point)
    (v/vega {:width 400 :height 400})
    (oz/v! :mode :vega))

(def bar-data
  [{:category "A", :amount 28} {:category "B", :amount 55} {:category "C", :amount 43} {:category "D", :amount 91} {:category "E", :amount 81} {:category "F", :amount 53} {:category "G", :amount 19} {:category "H", :amount 87}])

(def bar-chart
  {:axes [{:orient "bottom", :scale "xscale"}
          {:orient "left", :scale "yscale"}],
   :width 400,
   :scales [{:name "xscale", :type "band", :domain {:data "table", :field "category"}, :range "width", :padding 0.05, :round true}
            {:name "yscale", :domain {:data "table", :field "amount"}, :nice true, :range "height"}]
   :padding 5
   :marks [{:type "rect", :from {:data "table"},
            :encode {:enter {:x {:scale "xscale", :field "category"},
                             :width {:scale "xscale", :band 1}
                             , :y {:scale "yscale", :field "amount"}
                             , :y2 {:scale "yscale", :value 0}
                             }, :update {:fill {:value "steelblue"}},}}]
   , :$schema "https://vega.github.io/schema/vega/v4.json",
   :height 200,
   :data [{:name "table", :values bar-data}]})


(oz/v! bar-chart :mode :vega)

(-> (r/plot {:x :amount :y :category} (d/from-records bar-data))
    (r/layer ::r/bar)
    (v/vega {:width 400 :height 200})
    (oz/v! :mode :vega))
