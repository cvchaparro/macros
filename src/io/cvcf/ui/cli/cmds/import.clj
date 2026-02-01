(ns io.cvcf.ui.cli.cmds.import
  (:require
   [io.cvcf.macros.import.core :as i]
   [io.cvcf.macros.import.csv]
   [io.cvcf.macros.import.edn]
   [io.cvcf.macros.store :as s]))

(def commands
  [{:cmds       ["import" "foods"]
    :fn         #(i/handle-import % s/foods s/foods-imported?)
    :args->opts [:files]
    :spec       {:files {:coerce []}}}
   {:cmds       ["import" "fluids"]
    :fn         #(i/handle-import % s/fluids s/fluids-imported?)
    :args->opts [:files]
    :spec       {:files {:coerce []}}}
   {:cmds       ["import" "workouts"]
    :fn         #(i/handle-import % s/workouts s/workouts-imported?)
    :args->opts [:files]
    :spec       {:files {:coerce []}}}])
