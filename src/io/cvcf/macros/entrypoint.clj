(ns io.cvcf.macros.entrypoint
  (:require
   [babashka.cli :as cli]
   [io.cvcf.macros.import.core :as i]))

(def foods (atom nil))

(def commands
  [{:cmds ["import"]
    :fn #(->> %
              i/prepare
              (mapcat i/import*)
              (reset! foods))
    :args->opts [:files]
    :spec {:files {:coerce []}}}])

(defn -main
  [& args]
  (cli/dispatch commands args))
