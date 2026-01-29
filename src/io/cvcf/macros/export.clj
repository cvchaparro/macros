(ns io.cvcf.macros.export
  (:require
   [babashka.fs :as fs]
   [zprint.core :as zp]))

(def foods-format-options
  {:map {:comma? false
         :key-order [:id :title
                     :servings :calories
                     :protein :carbs :fat]}
   :style :justified})

(defn prepare
  [{:keys [opts]}]
  (let [{:keys [file]} opts]
    (fs/file "resources" file)))

(defn format-with-options
  [opts data]
  (zp/zprint-str data opts))

(defn export*
  [f data]
  (->> data
       (format-with-options foods-format-options)
       (spit f)))
