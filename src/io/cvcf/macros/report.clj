(ns io.cvcf.macros.report
  (:require
   [clojure.string :as str]
   [io.cvcf.macros.store :as s]
   [io.cvcf.macros.utils :as u]
   [tick.core :as t]))

(defn extract-duration-parts
  [d]
  (when d
    ((juxt t/hours
           #(mod (t/minutes %) 60)
           #(mod (t/seconds %) 60))
     d)))

(defn print-duration
  [d]
  (if-let [d (-> (cond
                   (nil? d)        nil
                   (string? d)     (u/->duration d)
                   (t/duration? d) d)
                 (extract-duration-parts))]
    (->> [:hrs :mins :secs]
         (zipmap d)
         (filter #(pos? (key %)))
         (map (fn [[x unit]] [x (name unit)]))
         (map #(str/join " " %))
         (str/join ", "))
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
  (let [ss (get-in (s/fluids-by-title) [title :servings])]
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
