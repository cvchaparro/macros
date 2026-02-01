(ns io.cvcf.macros.log
  (:require
   [clojure.string :as str]
   [io.cvcf.macros.defaults :as d]
   [io.cvcf.macros.store :as s]
   [io.cvcf.macros.utils :as u])
  (:import
   (java.util.regex Pattern)))

(def log-food-spec
  {:title    {:desc  "The food title."
              :alias :t
              :restrict [:id :query]}
   :id       {:desc  "The food id."
              :alias :i
              :restrict [:title :query]}
   :query    {:desc  "Search for a substring of the title."
              :alias :q
              :restrict [:title :id]}
   :servings {:desc     "The number of servings consumed."
              :alias    :s
              :required true
              :coerce   :double
              :default  1.0}})

(def log-fluid-spec
  (-> log-food-spec
      (update-vals #(u/replace-value % :desc #"food" "fluid"))))

(def log-workout-spec
  {:title        {:desc     "The workout title."
                  :alias    :t
                  :restrict [:id :query]}
   :id           {:desc     "The workout id."
                  :alias    :i
                  :restrict [:title :query]}
   :query        {:desc  "Search for a substring of the title."
                  :alias :q
                  :restrict [:title :id]}
   :duration     {:desc     "The duration spent doing the exercise."
                  :alias    :d
                  :validate u/valid-duration?}
   :set-duration {:desc     "The duration spent doing the set."
                  :alias    :sd
                  :validate u/valid-duration?}
   :sets         {:desc     "The number of sets."
                  :alias    :s}
   :reps         {:desc     "The number of reps in at least one set."
                  :alias    :r
                  :coerce   [:int]}
   :tags         {:desc     "Tag(s) associated with the food."
                  :coerce   []
                  :default  []}})

(def log-calories-spec
  {:cals  {:desc    "The calories burnt."
           :alias   :c
           :coerce  :double
           :require true}
   :units {:desc    "The calorie units."
           :alias   :u
           :require true
           :default d/default-calorie-unit}})

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

(defn find-food
  [opts]
  (find* (merge opts {:by-id (s/foods-by-id) :by-title (s/foods-by-title)})))

(defn log-food
  [{:keys [servings] :as opts}]
  (log* opts find-food s/print-food [:servings]))

(defn find-fluid
  [opts]
  (find* (merge opts {:by-id (s/fluids-by-id) :by-title (s/fluids-by-title)})))

(defn log-fluid
  [{:keys [servings] :as opts}]
  (log* opts find-fluid s/print-fluid [:servings]))

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
      (log* find-workout s/print-workout [:sets :duration :set-duration])))

(defn log-calories
  [{:keys [cals units] :or {units d/default-calorie-unit}}]
  (u/qty cals units))
