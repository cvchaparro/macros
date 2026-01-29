(ns user
  (:require
   [io.cvcf.macros.entrypoint :as e]))

(comment
  (nth @e/foods 99)

  (e/-main "import" "foods.csv" "crap.csv")
  (e/-main "export" "foods.edn")

  ::end)
