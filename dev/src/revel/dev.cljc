(ns revel.dev
  (:require [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [clojure.java.io :as io]
            [cheshire.core :refer [parse-string]]
            [revel.core :as r]
            [revel.vega :as v]
            [revel.data :as d]
            [oz.core :as oz]))

#_(oz/start-plot-server!)

(def cars-data
  (transform-keys ->kebab-case-keyword (parse-string (slurp (io/resource "cars.json")))))

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

(-> (r/plot {:x :horsepower :y :miles-per-gallon :size :acceleration} cars-data)
    (r/layer ::r/point)
    (v/vega {:width 400 :height 400})
    (oz/v! :mode :vega))
