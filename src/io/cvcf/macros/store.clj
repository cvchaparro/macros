(ns io.cvcf.macros.store
  (:require
   [clojure.string :as str]
   [io.cvcf.macros.arithmetic :as a]
   [io.cvcf.macros.utils :as u]))

(def ^:dynamic *foods-file*    "foods.edn")
(def ^:dynamic *fluids-file*   "fluids.edn")
(def ^:dynamic *workouts-file* "workouts.edn")

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

(defn combine
  [atom k by-id adder]
  (when-let [items (seq (k @atom))]
    (->> items
         (map #(assoc (get (by-id) ((comp str :id) %))
                      :n (u/qty (:servings %) :serving)))
         (#(if (> (count %) 1) (reduce adder %) (adder (first %)))))))

(defn update-stats
  []
  (let [foods  (combine log :foods foods-by-id a/add-macros)
        fluids (combine log :fluids fluids-by-id a/add-fluids)]
    (when foods
      (swap! log update-in [:stats :calories] assoc :in (:calories foods))
      (swap! log update-in [:stats] assoc :macros (select-keys foods [:protein :carbs :fat])))

    (when fluids
      (swap! log update-in [:stats] assoc :fluids (:servings fluids)))))

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
