(ns io.cvcf.macros.report
  (:require
   [clojure.string :as str]
   [io.cvcf.macros.store :refer [fluids-by-title]]
   [io.cvcf.macros.utils :as u]))

(defn print-duration
  [d]
  (if d
    (str/replace (u/->duration d) #"PT" " in ")
    ""))

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

(defn print-fluid
  [{:keys [title servings]}]
  (let [ss (get-in (fluids-by-title) [title :servings])]
    (println (format "%s: %.1f %s"
                     title
                     (* servings (u/amt ss))
                     (name (u/units ss))))))

(defn print-workout
  [{:keys [title sets]}]
  (println (str title ":"))
  (vec
   (map-indexed (fn [i {:keys [reps duration]}]
                  (println (format "set %d: %d reps%s" (inc i) reps (print-duration duration))))
                sets)))
