(ns user
  (:require
   [io.cvcf.ui.cli.entrypoint :as e]
   [io.cvcf.macros.import.core :as i]
   [io.cvcf.macros.new :as n]
   [io.cvcf.macros.store :as s]
   [tick.core :as t]))

(defn refresh-data
  [& {:keys [foods-fspec fluids-fspec workouts-fspec log-fspec
             foods fluids workouts log]
      :or   {foods-fspec s/*foods-file*
             fluids-fspec s/*fluids-file*
             workouts-fspec s/*workouts-file*
             log-fspec (n/make-log-fspec (t/today))
             foods s/foods
             fluids s/fluids
             workouts s/workouts
             log s/log}}]
  ;; Reload the foods
  (i/handle-import foods-fspec foods)

  ;; Reload the fluids
  (i/handle-import fluids-fspec fluids)

  ;; Reload the workouts
  (i/handle-import workouts-fspec workouts)

  ;; Load today's log
  (i/handle-import log-fspec log))

(comment
  (e/-main "import" "foods" "foods.csv" "crap.csv")
  (e/-main "import" "foods" "foods.edn")

  (e/-main "export" "foods.edn")

  (e/-main "new" "food"
           "-t" "Kate's Chicken and Rice"
           "-ss" "100" "-cal" "126" "-p" "11" "-c" "6.6" "-f" "5.5"
           "--tags" "soup" "--tags" "homemade")

  (e/-main "new" "log" "-s" "07:36")

  (e/-main "log" "food" "-q" "butter" "-u" "19")
  (e/-main "log" "fluid" "-q" "coffee" "-u" "483")

  (s/update-stats)

  (refresh-data)

  ::end)
