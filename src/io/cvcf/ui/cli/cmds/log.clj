(ns io.cvcf.ui.cli.cmds.log
  (:require
   [io.cvcf.macros.log :as l]
   [io.cvcf.macros.store :as s]))

(def commands
  [{:cmds ["log" "food"]
    :fn   (fn [{:keys [opts]}]
            (let [[food & others] (l/log-food opts)]
              (when-not others
                (swap! s/log update :foods conj food))))
    :spec l/log-food-spec}
   {:cmds ["log" "fluid"]
    :fn   (fn [{:keys [opts]}]
            (let [[fluid & others] (l/log-fluid opts)]
              (when-not others
                (swap! s/log update :fluids conj fluid))))
    :spec l/log-fluid-spec}
   {:cmds ["log" "workout"]
    :fn   (fn [{:keys [opts]}]
            (let [[workout & others] (l/log-workout opts)]
              (when-not others
                (swap! s/log update :workouts conj workout))))
    :spec l/log-workout-spec}
   {:cmds ["log" "cals"]
    :fn   (fn [{:keys [opts]}]
            (when-let [calories (l/log-calories opts)]
              (swap! s/log update-in [:stats :calories] assoc :out calories)))
    :spec l/log-calories-spec
    :args->opts [:cals]}])
