(ns io.cvcf.macros.import.csv
  (:require
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [io.cvcf.macros.import.core :as i]
   [io.cvcf.macros.new :as n]))

(defn process-row
  [fields]
  (let [[tt cals & others] fields
        [p c f] others]
    (if-let [[_ t ss su]
             (re-find #"(.*) \(([0-9\/\.]+) ?([A-Za-z ]+)\)" tt)]
      {:title t
       :serving-unit su :serving-size ss
       :calories cals :protein p :carbs c :fat f}
      (println "Error processing " tt))))

(defn csv-data->maps
  [data]
  (->> (rest data)
       (map process-row)
       (map n/new-food)))

(defmethod i/import* "csv"
  [input]
  (-> (io/reader input)
      csv/read-csv
      csv-data->maps))
