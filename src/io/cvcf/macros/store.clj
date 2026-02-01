(ns io.cvcf.macros.store
  (:require
   [clojure.string :as str]
   [io.cvcf.macros.arithmetic :as a]
   [io.cvcf.macros.utils :as u]))

(def ^:dynamic *foods-file*    "foods.edn")
(def ^:dynamic *fluids-file*   "fluids.edn")
(def ^:dynamic *workouts-file* "workouts.edn")

;; TODO: Explore making this metadata instead of different variables
(def foods-imported?    (atom false))
(def fluids-imported?   (atom false))
(def workouts-imported? (atom false))
(def foods-changed?     (atom false))
(def fluids-changed?    (atom false))
(def workouts-changed?  (atom false))

(def foods    (atom nil))
(def fluids   (atom nil))
(def workouts (atom nil))
(def log      (atom nil))

(defn by
  [a key-fn]
  (into {} (map (juxt key-fn identity) @a)))

(defn foods-by-id       [] (by foods (comp str :id)))
(defn fluids-by-id      [] (by fluids (comp str :id)))
(defn workouts-by-id    [] (by workouts (comp str :id)))
(defn foods-by-title    [] (by foods :title))
(defn fluids-by-title   [] (by fluids :title))
(defn workouts-by-title [] (by workouts :title))

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
  [{:keys [title servings] :as x}]
  (let [ss (get-in (fluids-by-title) [title :servings])]
    (println (format "%s: %.1f %s"
                     title
                     (* servings (u/amt ss))
                     (name (u/units ss))))))

(defn print-workout
  [{:keys [title sets] :as opts}]
  (println (str title ":"))
  (vec
   (map-indexed (fn [i {:keys [reps duration]}]
                  (let [duration (if duration (str/replace (u/->duration duration) #"PT" " in ") "")]
                    (println (format "set %d: %d reps%s" (inc i) reps duration))))
                sets)))

(defn combine
  [k by-id adder]
  (->> (k @log)
       (map #(assoc (get (by-id) ((comp str :id) %))
                    :n (u/qty (:servings %) :serving)))
       (reduce adder)))

(defn update-stats
  []
  (let [foods  (combine :foods foods-by-id a/add-macros)
        fluids (combine :fluids fluids-by-id a/add-fluids)]
    (swap! log update-in [:stats :calories] assoc :in (:calories foods))
    (swap! log update-in [:stats] assoc :macros (select-keys foods [:protein :carbs :fat]))
    (swap! log update-in [:stats] assoc :fluids (:servings fluids))))

(add-watch
 foods ::food-updated
 (fn [_ _ _ _]
   (when (and @foods-imported? (not @foods-changed?))
     (reset! foods-changed? true))))

(add-watch
 fluids ::fluid-updated
 (fn [_ _ _ _]
   (when (and @fluids-imported? (not @fluids-changed?))
     (reset! fluids-changed? true))))

(add-watch
 workouts ::workout-updated
 (fn [_ _ _ _]
   (when (and @workouts-imported? (not @workouts-changed?))
     (reset! workouts-changed? true))))
