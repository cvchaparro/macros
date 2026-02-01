(ns io.cvcf.ui.cli.cmds.log
  (:require
   [io.cvcf.macros.log :as l]
   [io.cvcf.macros.store :as s]))

(defn log-item
  [ks items]
  (let [[i & others] items
        ks (if (coll? ks) ks [ks])]
    (when-not others
      (swap! s/log update-in ks conj i))))

(defn apply-with-opts
  [f {:keys [opts]}]
  (f opts))

(def commands
  [{:cmds ["log" "food"]
    :fn   #(log-item :foods (apply-with-opts l/log-food %))
    :spec l/log-food-spec}
   {:cmds ["log" "fluid"]
    :fn   #(log-item :fluids (apply-with-opts l/log-fluid %))
    :spec l/log-fluid-spec}
   {:cmds ["log" "workout"]
    :fn   #(log-item :workouts (apply-with-opts l/log-workout %))
    :spec l/log-workout-spec}
   {:cmds ["log" "cals"]
    :fn   (fn [{:keys [opts]}]
            (when-let [calories (l/log-calories opts)]
              (swap! s/log update-in [:stats :calories] assoc :out calories)))
    :spec l/log-calories-spec
    :args->opts [:cals]}])
