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
  [f {:keys [create?]}]
  (if-let [[file] (seq (prepare {:opts {:files [f]}}))]
    (import* file)
    (if create?
      (println "File not found:" f)
      (do (-> f fs/parent fs/create-dirs)
          (spit f "{}")
          {}))))

(defn handle-import
  [f atom & {:keys [imported-flag? create?]}]
  (reset! atom (maybe-import f {:create? create?}))
  (when imported-flag?
    (reset! imported-flag? true)))
