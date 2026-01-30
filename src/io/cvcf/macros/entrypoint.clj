(ns io.cvcf.macros.entrypoint
  (:require
   [babashka.cli :as cli]
   [io.cvcf.macros.export :as e]
   [io.cvcf.macros.import.core :as i]
   [io.cvcf.macros.new :as n]))

(def foods (atom nil))
(def log   (atom nil))

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
    :args->opts [:file]}
   {:cmds ["new" "food"]
    :fn (fn [{:keys [opts]}]
          (swap! foods conj (n/new-food opts)))
    :spec n/new-food-spec}
   {:cmds ["new" "log"]
    :fn (fn [{:keys [opts]}]
          (reset! log (n/new-log opts)))
    :spec n/new-log-spec}])

(defn -main
  [& args]
  (cli/dispatch commands args))
