(set-env! :dependencies ['[seancorfield/boot-tools-deps "0.4.6" :scope "test"]
                         '[kixi/stats "0.5.0-SNAPSHOT"]]
          :checkouts ['[kixi/stats "0.5.0-SNAPSHOT"]])

(require '[boot-tools-deps.core :refer [deps]])

(deftask dev
  []
  (comp (deps :aliases [:dev])

        (repl)))
