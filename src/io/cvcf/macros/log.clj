(ns io.cvcf.macros.log
  (:require
   [clojure.string :as str]
   [io.cvcf.macros.store :as s])
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

(defn find-food
  [{:keys [title id query]}]
  (let [foods-by-title (s/foods-by-title)
        foods-by-id    (s/foods-by-id)]
    (cond
      (seq title) [(get foods-by-title title)]
      (seq id)    [(get foods-by-id    id)]
      (seq query)
      (let [re (Pattern/compile query Pattern/CASE_INSENSITIVE)]
        (->> (keys foods-by-title)
             (filter #(re-find re %))
             (map #(get foods-by-title %)))))))

(defn log-food
  [{:keys [servings] :as opts}]
  (let [[food & others] (find-food opts)]
    (cond
      others
      (do (println "Multiple results found. Nothing logged.\n")
          (doseq [{:keys [id title]} (into [food] others)]
            (->> [id title ""]
                 (str/join "\n")
                 println))
          (into [food] others))

      food
      (let [{:keys [id title]} food]
        (s/print-food (merge food {:servings servings}))
        [{:id id :title title :servings servings}])

      :else
      (println "Food not found. Nothing logged."))))
