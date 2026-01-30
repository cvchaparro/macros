(ns user
  (:require
   [clojure.repl.deps :refer [sync-deps]]
   [io.cvcf.macros.entrypoint :as e]))

(comment
  (nth @e/foods 99)

  (e/-main "import" "foods.csv" "crap.csv")
  (e/-main "export" "foods.edn")

  (e/-main "new" "food"
           "-t" "Kate's Chicken and Rice"
           "-ss" "100" "-cal" "126" "-p" "11" "-c" "6.6" "-f" "5.5"
           "--tags" "soup" "--tags" "homemade")

  (e/-main "new" "log")

  ::end)
