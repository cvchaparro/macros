(ns io.cvcf.macros.import.core
  (:require
   [babashka.fs :as fs]
   [clojure.java.io :as io]
   [io.cvcf.macros.utils :as u]))

(defmulti import* fs/extension)

(defn prepare
  [{:keys [opts]}]
  (let [{:keys [files]} opts]
    (->> files
         (map #(if (fs/exists? %) % (io/resource %)))
         (filter identity))))

(defn maybe-import
  [f {:keys [create?]}]
  (let [[file] (seq (prepare {:opts {:files [f]}}))
        d (if file (import* file) {})]
    (when create?
      (u/ensure-file-exists f)
      (spit f d))
    d))

(defn handle-import
  [f atom & {:keys [imported-flag? create?]}]
  (reset! atom (maybe-import f {:create? create?}))
  (when imported-flag?
    (reset! imported-flag? true))
  ::imported)
