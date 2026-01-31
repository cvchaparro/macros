(ns user
  (:require
   [clojure.repl.deps :refer [sync-deps]]
   [io.cvcf.macros.entrypoint :as e]
   [io.cvcf.macros.store :as s]))

(comment
  (e/-main "import" "foods.csv" "crap.csv")
  (e/-main "import" "foods.edn")

  (e/-main "export" "foods.edn")

  (e/-main "new" "food"
           "-t" "Kate's Chicken and Rice"
           "-ss" "100" "-cal" "126" "-p" "11" "-c" "6.6" "-f" "5.5"
           "--tags" "soup" "--tags" "homemade")

  (e/-main "new" "log" "-s" "07:36")

  (e/-main "log" "food" "-q" "Quest")

  s/log

  ::end)
