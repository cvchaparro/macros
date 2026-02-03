(ns io.cvcf.ui.cli.entrypoint
  (:require
   [babashka.cli :as cli]
   [io.cvcf.ui.cli.cmds.export :refer [commands] :rename {commands export-commands}]
   [io.cvcf.ui.cli.cmds.import :refer [commands] :rename {commands import-commands}]
   [io.cvcf.ui.cli.cmds.log :refer [commands] :rename {commands log-commands}]
   [io.cvcf.ui.cli.cmds.new :refer [commands] :rename {commands new-commands}]
   [io.cvcf.ui.cli.core :refer [with-log]]))

(def commands
  (concat
   import-commands
   export-commands
   new-commands
   log-commands))

(defn -main
  [& args]
  (with-log {}
    (cli/dispatch commands args)))
