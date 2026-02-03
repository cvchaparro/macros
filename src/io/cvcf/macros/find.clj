(ns io.cvcf.macros.find
  (:require
   [io.cvcf.macros.store :as s])
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

(defn find-food
  [opts]
  (find* (merge opts {:by-id (s/foods-by-id)
                      :by-title (s/foods-by-title)})))

(defn find-fluid
  [opts]
  (find* (merge opts {:by-id (s/fluids-by-id)
                      :by-title (s/fluids-by-title)})))

(defn find-workout
  [opts]
  (find* (merge opts {:by-id (s/workouts-by-id)
                      :by-title (s/workouts-by-title)})))
