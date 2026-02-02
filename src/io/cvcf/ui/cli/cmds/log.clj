(ns io.cvcf.ui.cli.cmds.log
  (:require
   [io.cvcf.macros.defaults :as d]
   [io.cvcf.macros.log :as l]
   [io.cvcf.macros.store :as s]
   [io.cvcf.macros.utils :as u]
   [tick.core :as t]))

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
              :coerce   :double
              :restrict [:units]}
   :units    {:desc     "The number of units consumed."
              :alias    :u
              :coerce   :double
              :restrict [:servings]}})

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
   :query        {:desc     "Search for a substring of the title."
                  :alias    :q
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
   :tags         {:desc     "Tag(s) associated with the workout."
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

(defn log-item
  [ks items & {:keys [timestamp] :or {timestamp (t/instant)}}]
  (let [[i & others] items
        ks (if (coll? ks) ks [ks])
        timestamp (if (u/timestamp? timestamp) timestamp (t/instant))]
    (when-not others
      (swap! s/log update-in ks conj (merge i {:at timestamp})))))

(def commands
  [{:cmds ["log" "food"]
    :fn   #(log-item :foods (l/log-food (:opts %)))
    :spec log-food-spec}
   {:cmds ["log" "fluid"]
    :fn   #(log-item :fluids (l/log-fluid (:opts %)))
    :spec log-fluid-spec}
   {:cmds ["log" "workout"]
    :fn   #(log-item :workouts (l/log-workout (:opts %)))
    :spec log-workout-spec}
   {:cmds ["log" "cals"]
    :fn   #(when-let [calories (l/log-calories (:opts %))]
             (swap! s/log update-in [:stats :calories] assoc :out calories))
    :spec log-calories-spec
    :args->opts [:cals]}])
