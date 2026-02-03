(ns io.cvcf.ui.cli.cmds.report
  (:require
   [io.cvcf.macros.utils :as u]
   [io.cvcf.macros.report :as r]))

(def show-day-spec
  {:day  {:desc     "The day for which to show data."
          :alias    :d
          :default  "today"
          :validate (some-fn #{"today" "yesterday"} u/time-or-date?)}
   :show {:desc     "The data to show in the report."
          :alias    :s
          :default  [:all]
          :coerce   [:keyword]
          :validate #(->> %
                          (map r/showable-data)
                          (filter identity))}})

(def commands
  [{:cmds ["show" "day"]
    :fn   #(r/show-day (:opts %))
    :spec show-day-spec}])
