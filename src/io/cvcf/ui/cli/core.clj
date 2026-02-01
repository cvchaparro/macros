(ns io.cvcf.ui.cli.core
  (:require
   [io.cvcf.macros.import.core :as i]
   [io.cvcf.macros.new :as n]
   [io.cvcf.macros.store :as s]
   [io.cvcf.macros.utils :as u]
   [tick.core :as t]))

(defmacro with-log
  [{:keys [foods-fspec fluids-fspec workouts-fspec date]
    :or   {foods-fspec    s/*foods-file*
           fluids-fspec   s/*fluids-file*
           workouts-fspec s/*workouts-file*
           date           (t/today)}}
   & body]
  `(let [foods-fspec#    (u/new-resource ~foods-fspec)
         fluids-fspec#   (u/new-resource ~fluids-fspec)
         workouts-fspec# (u/new-resource ~workouts-fspec)
         log-fspec#      ~(n/make-log-fspec date)]
     (try
       (i/handle-import foods-fspec# s/foods s/foods-imported?)
       (i/handle-import fluids-fspec# s/fluids s/fluids-imported?)
       (i/handle-import workouts-fspec# s/workouts s/workouts-imported?)
       (i/handle-import log-fspec# s/log {:create? true})

       ~@body

       (s/update-stats)
       (catch Exception e#
         (println e#))
       (finally
         (when @s/foods-changed?
           (e/export* foods-fspec# @s/foods))

         (when @s/fluids-changed?
           (e/export* fluids-fspec# @s/fluids))

         (when @s/workouts-changed?
           (e/export* workouts-fspec# @s/workouts))

         (e/export* log-fspec# @s/log)))))
