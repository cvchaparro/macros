(ns io.cvcf.macros.log
  (:require
   [clojure.string :as str]
   [io.cvcf.macros.defaults :as d]
   [io.cvcf.macros.report :as r]
   [io.cvcf.macros.store :as s]
   [io.cvcf.macros.utils :as u])
  (:import
   (java.util.regex Pattern)))

(defn find*
  [{:keys [title id query by-id by-title]}]
  (cond
    (seq title) [(get by-title title)]
    (seq id)    [(get by-id    id)]
    (seq query)
    (let [re (Pattern/compile query Pattern/CASE_INSENSITIVE)]
      (->> (keys by-title)
           (filter #(re-find re %))
           (map #(get by-title %))))))

(defn log*
  [opts finder printer ks]
  (let [[i & others] (finder opts)]
    (cond
      others
      (do (println "Multiple results found. Nothing logged.\n")
          (doseq [{:keys [id title]} (into [i] others)]
            (->> [id title ""]
                 (str/join "\n")
                 println))
          (into [i] others))

      i
      (let [{:keys [id title]} i
            selected (select-keys opts ks)]
        (printer (merge i selected))
        [(merge {:id id :title title} selected)])

      :else
      (println "Not found. Nothing logged."))))

(defn units->servings
  [{:keys [units] :as opts} finder]
  (when-let [[f & others] (finder opts)]
    (when-not others
      (/ units (u/amt (:servings f))))))

(defn find-food
  [opts]
  (find* (merge opts {:by-id (s/foods-by-id) :by-title (s/foods-by-title)})))

(defn log-food
  [{:keys [servings] :as opts}]
  (let [opts (assoc opts :servings (or servings (units->servings opts find-food)))]
    (log* opts find-food r/print-food [:servings])))

(defn find-fluid
  [opts]
  (find* (merge opts {:by-id (s/fluids-by-id) :by-title (s/fluids-by-title)})))

(defn log-fluid
  [{:keys [servings] :as opts}]
  (let [opts (assoc opts :servings (or servings (units->servings opts find-fluid)))]
    (log* opts find-fluid r/print-fluid [:servings])))

(defn find-workout
  [opts]
  (find* (merge opts {:by-id (s/workouts-by-id) :by-title (s/workouts-by-title)})))

(defn log-workout
  [{:keys [sets reps duration set-duration] :as opts}]
  (-> opts
      (assoc :sets (mapv #(merge {:reps %}
                                 (when set-duration
                                   {:duration set-duration}))
                         reps))
      (log* find-workout r/print-workout [:sets :duration :set-duration])))

(defn log-calories
  [{:keys [cals units] :or {units d/default-calorie-unit}}]
  (u/qty cals units))
