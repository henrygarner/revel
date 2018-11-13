(set-env! :dependencies ['[seancorfield/boot-tools-deps "0.4.6" :scope "test"]])

(require '[boot-tools-deps.core :refer [deps]])

(deftask dev
  []
  (comp (deps :aliases [:dev])
        (repl)))
