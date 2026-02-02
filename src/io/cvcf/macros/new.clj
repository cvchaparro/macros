(ns io.cvcf.macros.new
  (:require
   [babashka.fs :as fs]
   [clojure.string :as str]
   [io.cvcf.macros.defaults :as d]
   [io.cvcf.macros.utils :as u]
   [tick.core :as t]))

(def new-food-spec
  {:title        {:desc     "The food title."
                  :alias    :t
                  :require  true}
   :serving-unit {:desc     "The serving size units."
                  :alias    :su
                  :require  true
                  :coerce   :keyword
                  :default  d/default-serving-unit}
   :serving-size {:desc     "The serving size amount."
                  :alias    :ss
                  :require  true}
   :calorie-unit {:desc     "The units used to measure calories."
                  :alias    :cu
                  :require  true
                  :coerce   :keyword
                  :default  d/default-calorie-unit}
   :calories     {:desc     "The calories in one serving."
                  :alias    :cal
                  :require  true
                  :coerce   :double
                  :validate (comp not neg?)}
   :macros-unit  {:desc     "The units used to measure macros."
                  :alias    :mu
                  :require  true
                  :coerce   :keyword
                  :default  d/default-macros-unit}
   :protein      {:desc     "The protein in one serving."
                  :alias    :p
                  :default  0
                  :coerce   :double
                  :validate (comp not neg?)}
   :carbs        {:desc     "The carbs in one serving."
                  :alias    :c
                  :default  0
                  :coerce   :double
                  :validate (comp not neg?)}
   :fat          {:desc     "The fat in one serving."
                  :alias    :f
                  :default  0
                  :coerce   :double
                  :validate (comp not neg?)}
   :tags         {:desc     "Tag(s) associated with the food."
                  :coerce   []
                  :default  []}})

(def new-fluid-spec
  (-> new-food-spec
      (select-keys [:title :serving-size :serving-unit :tags])
      (update-vals #(u/replace-value % :desc #"food" "fluid"))
      (assoc-in [:serving-unit :default] d/default-fluids-unit)))

(def new-workout-spec
  (-> new-food-spec
      (select-keys [:title :tags])
      (assoc :exercises
             {:desc    "A list of exercises performed in the workout."
              :alias   :e
              :coerce  []
              :default []})
      (update-vals #(u/replace-value % :desc #"food" "workout"))))

(def new-exercise-spec
  (-> new-food-spec
      (select-keys [:title :tags])
      (assoc :muscle-groups
             {:desc    "A list of muscle groups worked during the exercise."
              :alias   :m
              :coerce  []
              :default []})
      (assoc :equipment
             {:desc    "A list of equipment required to perform the exercise."
              :alias   :e
              :coerce  []
              :default []})
      (update-vals #(u/replace-value % :desc #"food" "exercise"))))

(def new-log-spec
  {:title {:desc     "The log title."
           :alias    :t
           :require  true
           :default  (str (t/today))}
   :sleep {:desc     "The sleep duration."
           :alias    :s
           :require  true
           :validate u/valid-duration?}
   :date  {:desc     "The date of the log."
           :alias    :d}})

(defn split-tags
  [tags]
  (if (seq tags)
    (->> tags
         (mapcat #(if (string? %) (str/split % #",") %))
         (mapv u/->keyword))
    []))

(defn new-food
  [{:keys [id title
           serving-size serving-unit
           calorie-unit calories
           macros-unit protein carbs fat tags]
    :or {id (random-uuid)
         serving-unit d/default-serving-unit
         calorie-unit d/default-calorie-unit
         macros-unit d/default-macros-unit}}]
  {:id id :tags (split-tags tags) :title title
   :servings (u/qty serving-size serving-unit)
   :calories (u/qty calories calorie-unit)
   :protein  (u/qty protein macros-unit)
   :carbs    (u/qty carbs macros-unit)
   :fat      (u/qty fat macros-unit)})

(defn new-fluid
  [{:keys [id title serving-size serving-unit tags]
    :or   {id (random-uuid) serving-unit d/default-fluids-unit}}]
  {:id id :tags (split-tags tags) :title title
   :servings (u/qty serving-size serving-unit)})

(defn new-exercise
  [{:keys [id title tags muscle-groups equipment]
    :or   {id (random-uuid)}}]
  {:id id :tags (split-tags tags) :title title
   :muscle-groups (split-tags muscle-groups)
   :equipment (split-tags equipment)})

(defn new-workout
  [{:keys [id title exercises tags]
    :or   {id    (random-uuid)
           title (str "Workout: " (t/today))}}]
  {:id id :tags (split-tags tags) :title title
   :exercises []})

(defn make-log-fspec
  [date]
  (doto (->> (str date)
             (#(str/split % #"-"))
             (into ["logs"])
             (str/join fs/file-separator)
             (#(str % ".edn")))
    (u/ensure-file-exists)))

(defn new-log
  [{:keys [id title date sleep file]
    :or   {id    (random-uuid)
           date  (t/inst)
           file  (make-log-fspec (t/today))
           title (str (t/today))}}]
  (u/ensure-file-exists file)
  {:id       id
   :title    title
   :date     date
   :stats    {:calories {:in  (u/qty 0 d/default-calorie-unit)
                         :out (u/qty 0 d/default-calorie-unit)}
              :macros   {:protein (u/qty 0 d/default-macros-unit)
                         :carbs   (u/qty 0 d/default-macros-unit)
                         :fat     (u/qty 0 d/default-macros-unit)}
              :fluids   (u/qty 0 d/default-fluids-unit)
              :sleep    (u/->duration sleep)}
   :foods    []
   :fluids   []
   :workouts []})
