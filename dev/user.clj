(ns user
  (:require
   [clojure.java.io :as io]
   [io.cvcf.macros.import :as i]))

(def foods (atom nil))

(comment

  (->> (or (io/resource "foods.edn")
           (io/resource "foods.csv"))
       i/import*
       (reset! foods))

  ::end)
