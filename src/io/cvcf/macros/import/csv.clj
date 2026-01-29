(ns io.cvcf.macros.import.csv
  (:require
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [io.cvcf.macros.import.core :as i]
   [io.cvcf.macros.import.defaults :as defaults]))

(defn qty
  ([amount] (qty amount nil))
  ([amount units] {:amount amount
                   :units (if-not (keyword? units)
                            (-> units (str/replace #" " "-") keyword)
                            units)}))

(defn preprocess-header
  [fields]
  (->> (rest fields)
       (map str/trim)
       (map str/lower-case)
       (map #(str/replace % #"\([a-z]+\)" ""))
       (map #(str/replace % #"[^A-z0-9-]" ""))
       (map keyword)
       (into [:id :servings :tags :title])))

(defn preprocess-row
  [fields]
  (let [title-re #"(.*) \(([0-9\/\.]+) ?([A-z ]+)\)"
        [_ title amt unit] (re-find title-re (first fields))
        servings (parse-double amt)
        calories (parse-double (second fields))]
    (-> [(random-uuid)
         (qty servings unit)
         []
         title
         (qty calories defaults/calorie-unit)]
        (into (->> (nthrest fields 2)
                   (map parse-double)
                   (map #(into (qty % defaults/macros-unit))))))))

(defn csv-data->maps
  [data]
  (map zipmap
       (->> (first data)
            preprocess-header
            repeat)
       (->> (rest data)
            (map preprocess-row))))

(defmethod i/import* "csv"
  [input]
  (-> (io/reader input)
      csv/read-csv
      csv-data->maps))
