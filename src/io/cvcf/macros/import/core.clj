(ns io.cvcf.macros.import.core
  (:require
   [babashka.fs :as fs]))

(defmulti import* fs/extension)
