(ns io.cvcf.macros.report
  (:require
   [clojure.string :as str]
   [io.cvcf.macros.arithmetic :as a]
   [io.cvcf.macros.find :as f]
   [io.cvcf.macros.import.core :as i]
   [io.cvcf.macros.new :as n]
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
  [{:keys [title calories protein carbs fat]} {:keys [servings]}]
  (println (format "%s: %.1f %s (%.1f%s p, %.1f%s c, %.1f%s f)"
                   title
                   (u/amt (a/times servings calories))
                   (name (u/units calories))
                   (u/amt (a/times servings protein))
                   (name (u/units protein))
                   (u/amt (a/times servings carbs))
                   (name (u/units carbs))
                   (u/amt (a/times servings fat))
                   (name (u/units fat)))))

(defn print-fluid
  [{:keys [title]} {:keys [servings]}]
  (let [ss (get-in (s/fluids-by-title) [title :servings])]
    (println (format "%s: %.1f %s"
                     title
                     (u/amt (a/times servings ss))
                     (name (u/units ss))))))

(defn print-workout
  [{:keys [title sets]}]
  (println (str title ":"))
  (vec
   (map-indexed (fn [i {:keys [reps duration]}]
                  (println (format "set %d: %d reps%s" (inc i) reps (print-duration duration))))
                sets)))

(def showable-data #{:all :foods :fluids :workouts :stats})

(defn get-date
  [d]
  (cond (#{"today" "yesterday"} d) ((resolve (symbol "tick.core" d)))
        (u/time-or-date? d)      d
        :else                    (t/today)))

(defn print-total-macros
  [{:keys [calories protein carbs fat] :as macros}]
  (if macros
    (format "%.1f %s :: (%.1f%s p, %.1f%s c, %.1f%s f)"
            (u/amt calories) (name (u/units calories))
            (u/amt protein)  (name (u/units protein))
            (u/amt carbs)    (name (u/units carbs))
            (u/amt fat)      (name (u/units fat)))
    "Nothing to display. Add some foods and try again."))

(defn print-total-fluids
  [{:keys [servings] :as fluids}]
  (if fluids
    (format "%.1f L (240 mL servings: %.1f)"
            (/ (u/amt servings) 1000.0)
            (/ (u/amt servings) 240.0))
    "Nothing to display. Add some fluids and try again."))

(defn print-logged-entries
  [entries {:keys [title by-id finder printer adder totaler]}]
  (when (seq entries)
    (println (str "\n" title ":"))
    (doseq [entry entries]
      (print (str "- (" (t/format "HH:mm:ss" (t/date-time (:at entry))) "): "))
      (let [[e] (finder entry)]
        (printer e entry)))
    (->> (s/combine entries by-id adder)
         totaler
         (println "\n=> Totals:"))))

(defn show-day
  [{:keys [day show] :or {day "today" show [:all]}}]
  (let [day-fspec (n/make-log-fspec (get-date day))
        show (if (some #{:all} show)
               (remove #{:all} showable-data)
               show)
        {:keys [title foods fluids workouts stats]}
        (-> day-fspec
            (i/maybe-import {})
            (select-keys (conj show :title)))]

    (println title)
    (print-logged-entries foods
                          {:title "Foods"
                           :finder f/find-food
                           :printer print-food
                           :adder a/add-macros
                           :by-id s/foods-by-id
                           :totaler print-total-macros})
    (print-logged-entries fluids
                          {:title "Fluids"
                           :finder f/find-fluid
                           :printer print-fluid
                           :adder a/add-fluids
                           :by-id s/fluids-by-id
                           :totaler print-total-fluids})))
