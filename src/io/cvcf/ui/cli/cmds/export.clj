(ns io.cvcf.ui.cli.cmds.export
  (:require
   [io.cvcf.macros.export :as e]
   [io.cvcf.macros.store :as s]))

(def commands
  [{:cmds       ["export"]
    :fn         #(-> % e/prepare (e/export* (vec @s/foods)))
    :args->opts [:file]}])
