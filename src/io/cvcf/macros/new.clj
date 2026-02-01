(ns io.cvcf.macros.new
  (:require
   [clojure.string :as str]
   [io.cvcf.macros.utils :as u]
   [tick.core :as t]))

(def default-serving-unit :g)
(def default-calorie-unit :kcal)
(def default-macros-unit  :g)
(def default-fluids-unit  :mL)

(def new-food-spec
  {:title        {:desc     "The food title."
                  :alias    :t
                  :require  true}
   :serving-unit {:desc     "The serving size units."
                  :alias    :su
                  :require  true
                  :coerce   :keyword
                  :default  default-serving-unit}
   :serving-size {:desc     "The serving size amount."
                  :alias    :ss
                  :require  true}
   :calorie-unit {:desc     "The units used to measure calories."
                  :alias    :cu
                  :require  true
                  :coerce   :keyword
                  :default  default-calorie-unit}
   :calories     {:desc     "The calories in one serving."
                  :alias    :cal
                  :require  true
                  :coerce   :double
                  :validate (comp not neg?)}
   :macros-unit  {:desc     "The units used to measure macros."
                  :alias    :mu
                  :require  true
                  :coerce   :keyword
                  :default  default-macros-unit}
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
      (assoc-in [:serving-unit :default] default-fluids-unit)))

(def new-workout-spec
  (-> new-food-spec
      (select-keys [:title :tags])
      (update-vals #(u/replace-value % :desc #"food" "workout"))))

(def new-log-spec
  {:title {:desc    "The log title."
           :alias   :t
           :require true
           :default (str (t/date))}
   :sleep {:desc    "The sleep duration."
           :alias   :s
           :require true
           :validate u/valid-duration?}})

(defn split-tags
  [tags]
  (if (seq tags)
    (->> (if (string? tags) (str/split tags #",") tags)
         (mapv u/->keyword))
    []))

(defn new-food
  [{:keys [id title
           serving-size serving-unit
           calorie-unit calories
           macros-unit protein carbs fat tags]
    :or {id (random-uuid)
         serving-unit default-serving-unit
         calorie-unit default-calorie-unit
         macros-unit default-macros-unit}}]
  {:id id :tags (split-tags tags) :title title
   :servings (u/qty serving-size serving-unit)
   :calories (u/qty calories calorie-unit)
   :protein  (u/qty protein macros-unit)
   :carbs    (u/qty carbs macros-unit)
   :fat      (u/qty fat macros-unit)})

(defn new-fluid
  [{:keys [id title serving-size serving-unit tags]
    :or   {id (random-uuid) serving-unit default-fluids-unit}}]
  {:id id :tags (split-tags tags) :title title
   :servings (u/qty serving-size serving-unit)})

(defn new-workout
  [{:keys [id title tags]
    :or   {id (random-uuid)}}]
  {:id id :tags (split-tags tags) :title title})

(defn new-log
  [{:keys [id title date sleep]
    :or   {id   (random-uuid)
           date (t/inst)}}]
  {:id       id
   :title    title
   :date     date
   :stats    {:calories {:in  (u/qty 0 default-calorie-unit)
                         :out (u/qty 0 default-calorie-unit)}
              :macros   {:protein (u/qty 0 default-macros-unit)
                         :carbs   (u/qty 0 default-macros-unit)
                         :fat     (u/qty 0 default-macros-unit)}
              :fluids   (u/qty 0 default-fluids-unit)
              :sleep    (u/->duration sleep)}
   :foods    []
   :fluids   []
   :workouts []})
