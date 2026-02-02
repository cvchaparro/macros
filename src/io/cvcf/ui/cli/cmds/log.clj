(ns io.cvcf.ui.cli.cmds.log
  (:require
   [io.cvcf.macros.log :as l]
   [io.cvcf.macros.store :as s]
   [io.cvcf.macros.utils :as u]
   [tick.core :as t]))

(defn log-item
  [ks items & {:keys [timestamp] :or {timestamp (t/instant)}}]
  (let [[i & others] items
        ks (if (coll? ks) ks [ks])
        timestamp (if (u/timestamp? timestamp) timestamp (t/instant))]
    (when-not others
      (swap! s/log update-in ks conj (merge i {:at timestamp})))))

(def commands
  [{:cmds ["log" "food"]
    :fn   #(log-item :foods (l/log-food (:opts %)))
    :spec l/log-food-spec}
   {:cmds ["log" "fluid"]
    :fn   #(log-item :fluids (l/log-fluid (:opts %)))
    :spec l/log-fluid-spec}
   {:cmds ["log" "workout"]
    :fn   #(log-item :workouts (l/log-workout (:opts %)))
    :spec l/log-workout-spec}
   {:cmds ["log" "cals"]
    :fn   #(when-let [calories (l/log-calories (:opts %))]
             (swap! s/log update-in [:stats :calories] assoc :out calories))
    :spec l/log-calories-spec
    :args->opts [:cals]}])
