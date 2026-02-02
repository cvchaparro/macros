(ns io.cvcf.ui.cli.cmds.new
  (:require
   [io.cvcf.macros.defaults :as d]
   [io.cvcf.macros.new :as n]
   [io.cvcf.macros.store :as s]
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

(def commands
  [{:cmds ["new" "food"]
    :fn   (fn [{:keys [opts]}]
            (let [food (n/new-food opts)]
              (swap! s/foods conj food)))
    :spec new-food-spec}
   {:cmds ["new" "fluid"]
    :fn   (fn [{:keys [opts]}]
            (let [fluid (n/new-fluid opts)]
              (swap! s/fluids conj fluid)))
    :spec new-fluid-spec}
   {:cmds ["new" "workout"]
    :fn   (fn [{:keys [opts]}]
            (let [workout (n/new-workout opts)]
              (swap! s/workouts conj workout)))
    :spec new-workout-spec}
   {:cmds       ["new" "log"]
    :fn         (fn [{:keys [opts]}]
                  (reset! s/log (n/new-log opts)))
    :spec       new-log-spec
    :args->opts [:file]}])
