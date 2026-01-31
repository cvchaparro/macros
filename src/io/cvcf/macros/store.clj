(ns io.cvcf.macros.store
  (:require
   [io.cvcf.macros.utils :as u]))

(def foods (atom nil))
(def log   (atom nil))

(defn foods-by
  [key-fn]
  (into {} (map (juxt key-fn identity) @foods)))

(defn foods-by-id    [] (foods-by (comp str :id)))
(defn foods-by-title [] (foods-by :title))

(defn print-food
  [{:keys [title servings calories protein carbs fat]}]
  (println (format "%s: %.1f %s (%.1f%s p, %.1f%s c, %.1f%s f)"
                   title
                   (* servings (u/amt calories))
                   (name (u/units calories))
                   (* servings (u/amt protein))
                   (name (u/units protein))
                   (* servings (u/amt carbs))
                   (name (u/units carbs))
                   (* servings (u/amt fat))
                   (name (u/units fat)))))
