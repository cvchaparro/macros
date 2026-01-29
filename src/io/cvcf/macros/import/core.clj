(ns io.cvcf.macros.import.core
  (:require
   [babashka.fs :as fs]
   [clojure.java.io :as io]))

(defmulti import* fs/extension)

(defn prepare
  [{:keys [opts]}]
  (let [{:keys [files]} opts]
    (map #(if (fs/exists? %) % (io/resource %)) files)))
