(ns io.cvcf.macros.import.core
  (:require
   [babashka.fs :as fs]
   [clojure.java.io :as io]))

(defmulti import* fs/extension)

(defn prepare
  [{:keys [opts]}]
  (let [{:keys [files]} opts]
    (->> files
         (map #(if (fs/exists? %) % (io/resource %)))
         (filter identity))))

(defn maybe-import
  [f]
  (when-let [[file] (seq (prepare {:opts {:files [f]}}))]
    (import* file)))
