(ns user
  (:require
   [clojure.repl.deps :refer [sync-deps]]
   [io.cvcf.macros.entrypoint :as e]))

(comment
  (nth @e/foods 99)

  (e/-main "import" "foods.csv" "crap.csv")
  (e/-main "export" "foods.edn")

  ::end)
