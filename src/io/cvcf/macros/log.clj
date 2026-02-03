(ns io.cvcf.macros.log
  (:require
   [clojure.string :as str]
   [io.cvcf.macros.defaults :as d]
   [io.cvcf.macros.find :as f]
   [io.cvcf.macros.utils :as u]))

(defn log*
  [opts finder ks]
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
        (println (merge {:id id :title title} selected))
        [(merge {:id id :title title} selected)])

      :else
      (println "Not found. Nothing logged."))))

(defn units->servings
  [{:keys [units] :as opts} finder]
  (when units
    (when-let [[f & others] (finder opts)]
      (when-not others
        (/ units (u/amt (:servings f)))))))

(defn log-food
  [{:keys [servings] :as opts}]
  (let [opts (assoc opts :servings
                    (or servings
                        (units->servings opts f/find-food)
                        1.0))]
    (log* opts f/find-food [:servings])))

(defn log-fluid
  [{:keys [servings] :as opts}]
  (let [opts (assoc opts :servings
                    (or servings
                        (units->servings opts f/find-fluid)
                        1.0))]
    (log* opts f/find-fluid [:servings])))

(defn log-workout
  [{:keys [sets reps duration set-duration] :as opts}]
  (-> opts
      (assoc :sets (mapv #(merge {:reps %}
                                 (when set-duration
                                   {:duration set-duration}))
                         reps))
      (log* f/find-workout [:sets :duration :set-duration])))

(defn log-calories
  [{:keys [cals units] :or {units d/default-calorie-unit}}]
  (u/qty cals units))
