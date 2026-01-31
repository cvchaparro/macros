(ns io.cvcf.macros.log
  (:require
   [clojure.string :as str]
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
  [{:keys [servings] :as opts} finder printer]
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
      (let [{:keys [id title]} i]
        (printer (merge i {:servings servings}))
        [{:id id :title title :servings servings}])

      :else
      (println "Not found. Nothing logged."))))

(defn find-food
  [opts]
  (find* (merge opts {:by-id (s/foods-by-id) :by-title (s/foods-by-title)})))

(defn log-food
  [{:keys [servings] :as opts}]
  (log* opts find-food s/print-food))

(defn find-fluid
  [opts]
  (find* (merge opts {:by-id (s/fluids-by-id) :by-title (s/fluids-by-title)})))

(defn log-fluid
  [{:keys [servings] :as opts}]
  (log* opts find-fluid s/print-fluid))
