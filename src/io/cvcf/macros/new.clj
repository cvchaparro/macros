(ns io.cvcf.macros.new
  (:require
   [clojure.string :as str]
   [tick.core :as t]))

(def default-serving-unit :g)
(def default-calorie-unit :kcal)
(def default-macros-unit :g)

(def new-food-spec
  {:title        {:desc    "The food title."
                  :alias   :t
                  :require true}
   :serving-unit {:desc    "The serving size units."
                  :alias   :su
                  :require true
                  :coerce   :keyword
                  :default default-serving-unit}
   :serving-size {:desc    "The serving size amount."
                  :alias   :ss
                  :require true}
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

(def new-log-spec
  {:title {:desc    "The log title."
           :alias   :t
           :require true
           :default (str (t/date))}})

(defn ->double
  [x]
  (cond
    (string? x)  (parse-double x)
    (keyword? x) (parse-double (name x))
    :else        x))

(defn ->keyword
  [x]
  (cond
    (string? x) (-> x (str/replace #" " "-") keyword)
    (symbol? x) (keyword x)
    :else       x))

(defn qty
  [amount units]
  {:amount (->double amount) :units (->keyword units)})

(defn split-tags
  [tags]
  (if (seq tags)
    (->> (if (string? tags) (str/split tags #",") tags)
         (map ->keyword))
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
   :servings (qty serving-size serving-unit)
   :calories (qty calories calorie-unit)
   :protein  (qty protein macros-unit)
   :carbs    (qty carbs macros-unit)
   :fat      (qty fat macros-unit)})

(defn new-log
  [{:keys [id title date]
    :or {id (random-uuid)
         date (t/instant)}}]
  {:id id
   :title title
   :date date
   :foods []
   :fluids []
   :workouts []})
