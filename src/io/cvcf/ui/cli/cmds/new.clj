(ns io.cvcf.ui.cli.cmds.new
  (:require
   [io.cvcf.macros.new :as n]
   [io.cvcf.macros.store :as s]))

(def commands
  [{:cmds ["new" "food"]
    :fn   (fn [{:keys [opts]}]
            (let [food (n/new-food opts)]
              (swap! s/foods conj food)))
    :spec n/new-food-spec}
   {:cmds ["new" "fluid"]
    :fn   (fn [{:keys [opts]}]
            (let [fluid (n/new-fluid opts)]
              (swap! s/fluids conj fluid)))
    :spec n/new-fluid-spec}
   {:cmds ["new" "workout"]
    :fn   (fn [{:keys [opts]}]
            (let [workout (n/new-workout opts)]
              (swap! s/workouts conj workout)))
    :spec n/new-workout-spec}
   {:cmds       ["new" "log"]
    :fn         (fn [{:keys [opts]}]
                  (reset! s/log (n/new-log opts)))
    :spec       n/new-log-spec
    :args->opts [:file]}])
