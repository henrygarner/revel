(ns revel.dev
  (:require [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [cheshire.core :refer [parse-string]]
            [kixi.stats.core :as kixi]
            [kixi.stats.distribution :as dist]
            [revel.core :as r]
            [revel.vega :as v]
            [revel.data :as d]
            [oz.core :as oz]))

(def hist-data
  (dist/sample 1000 (dist/normal {:mu 100 :sd 25})))

(defn test-data
  []
  (first (str/split (slurp "/Users/henry/Desktop/test.tsv") #"\n")))

(def scatter-data
  (map #(hash-map :x % :y (dist/draw (dist/normal {:mu (* % 0.5) :sd 10}))) (take 10 hist-data)))

#_(oz/start-plot-server!)

(def cars-data
  (take 1000 (transform-keys ->kebab-case-keyword (parse-string (slurp (io/resource "cars.json"))))))


#_(-> (r/plot {:x :horsepower :y :miles-per-gallon :size :acceleration} (d/from-records cars-data))
    (r/layer ::r/point)
    (r/render ::v/vega {:width 400 :height 400})
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


#_(oz/v! bar-chart :mode :vega)

#_(-> (r/plot {:x :amount :y :category})
      (r/layer ::r/point)
      (r/render ::v/vega (d/from-records bar-data) {:width 400 :height 200})
      #_(oz/v! :mode :vega))

(def dataset
  (d/from-records (map #(hash-map :x %1 :y %2) hist-data (shuffle hist-data))))

#_(-> (r/plot {:x :x :y :y})
    (r/layer ::r/point)
    (r/layer ::r/linear-model)
    (r/data (d/from-records scatter-data))
    (r/render ::v/vega {:width 400 :height 400})
    (oz/v! :mode :vega))

#_(spit "/Users/henry/Desktop/test.tsv" (apply str (map (fn [{:keys [x y]}] (str x "\t" y "\n")) scatter-data)))

#_(-> (r/plot {:x :x})
    (r/layer ::r/histogram)
    (r/data dataset)
    (r/render ::v/vega {:width 400 :height 400})
    (oz/v! :mode :vega)
    )

#_(-> (r/plot {:x :x})
      (r/layer ::r/histogram)
      (r/render ::v/vega (d/from-records (map (partial hash-map :x) hist-data)) {:width 400 :height 200})
      (oz/v! :mode :vega))
