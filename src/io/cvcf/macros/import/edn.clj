(ns io.cvcf.macros.import.edn
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [io.cvcf.macros.import.core :as i]))

(defmethod i/import* "edn"
  [input]
  (-> (io/reader input)
      java.io.PushbackReader.
      edn/read))
