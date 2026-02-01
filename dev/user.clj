(ns user
  (:require
   [io.cvcf.ui.cli.entrypoint :as e]
   [io.cvcf.macros.import.core :as i]
   [io.cvcf.macros.new :as n]
   [io.cvcf.macros.store :as s]
   [tick.core :as t]))

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

  ;; Reload the foods
  (i/handle-import s/*foods-file* s/foods)

  ;; Reload the fluids
  (i/handle-import s/*fluids-file* s/fluids)

  ;; Reload the workouts
  (i/handle-import s/*workouts-file* s/workouts)

  ;; Load today's log
  (-> (t/today)
      n/make-log-fspec
      (i/handle-import s/log))

  ::end)
