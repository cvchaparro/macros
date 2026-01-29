(ns io.cvcf.macros.import
  (:require
   [babashka.fs :as fs]
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [clojure.string :as str]))

(def calorie-unit :kcal)
(def macros-unit :g)

(defmulti import* fs/extension)

(defn preprocess-header
  [fields]
  (->> (rest fields)
       (map str/trim)
       (map str/lower-case)
       (map #(str/replace % #"\([a-z]+\)" ""))
       (map #(str/replace % #"[^A-z0-9-]" ""))
       (map keyword)
       (into [:id :servings :tags :title])))

(defn qty
  ([amount] (qty amount nil))
  ([amount units] {:amount amount
                   :units (if-not (keyword? units)
                            (-> units (str/replace #" " "-") keyword)
                            units)}))

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
         (qty calories calorie-unit)]
        (into (->> (nthrest fields 2)
                   (map parse-double)
                   (map #(into (qty % macros-unit))))))))

(defn csv-data->maps
  [data]
  (map zipmap
       (->> (first data)
            preprocess-header
            repeat)
       (->> (rest data)
            (map preprocess-row))))

(defmethod import* "csv"
  [input]
  (-> (io/reader input)
      csv/read-csv
      csv-data->maps))

(comment

  (concat
   (import* "/home/cvc/downloads/Nutrition - Foods.csv")
   (import* "/home/cvc/downloads/Nutrition - Crap.csv"))

  ::end)
