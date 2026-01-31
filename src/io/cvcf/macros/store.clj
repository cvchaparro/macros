(ns io.cvcf.macros.store
  (:require
   [io.cvcf.macros.arithmetic :as a]
   [io.cvcf.macros.utils :as u]))

(def ^:dynamic *foods-file* "foods.edn")

;; TODO: Explore making this metadata instead of different variables
(def foods-imported? (atom false))
(def foods-changed?  (atom false))

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

(defn update-stats
  []
  (let [all (->> (:foods @log)
                 (map #(assoc (get (foods-by-id) ((comp str :id) %))
                              :n
                              (u/qty (:servings %) :serving)))
                 (reduce a/add-macros))]
    (swap! log update-in [:stats :calories] assoc :in (:calories all))
    (swap! log update-in [:stats] assoc :macros (select-keys all [:protein :carbs :fat]))))

(add-watch
 foods ::food-updated
 (fn [_ _ _ _]
   (when (and @foods-imported? (not @foods-changed?))
     (reset! foods-changed? true))))
