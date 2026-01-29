(ns io.cvcf.macros.entrypoint
  (:require
   [babashka.cli :as cli]
   [io.cvcf.macros.import.core :as i]
   [io.cvcf.macros.import.csv]
   [io.cvcf.macros.import.edn]
   [io.cvcf.macros.export :as e]))

(def foods (atom nil))

(def commands
  [{:cmds ["import"]
    :fn #(->> %
              i/prepare
              (mapcat i/import*)
              (reset! foods))
    :args->opts [:files]
    :spec {:files {:coerce []}}}
   {:cmds ["export"]
    :fn #(-> %
             e/prepare
             (e/export* (vec @foods)))
    :args->opts [:file]}])

(defn -main
  [& args]
  (cli/dispatch commands args))
