(ns io.cvcf.macros.entrypoint
  (:require
   [babashka.cli :as cli]
   [babashka.fs :as fs]
   [clojure.string :as str]
   [io.cvcf.macros.arithmetic :as a]
   [io.cvcf.macros.export :as e]
   [io.cvcf.macros.import.core :as i]
   [io.cvcf.macros.import.csv]
   [io.cvcf.macros.import.edn]
   [io.cvcf.macros.log :as l]
   [io.cvcf.macros.new :as n]
   [io.cvcf.macros.store :as s]
   [io.cvcf.macros.utils :as u]
   [tick.core :as t]))

(def commands
  [{:cmds       ["import" "foods"]
    :fn         #(i/handle-import % s/foods s/foods-imported?)
    :args->opts [:files]
    :spec       {:files {:coerce []}}}
   {:cmds       ["import" "fluids"]
    :fn         #(i/handle-import % s/fluids s/fluids-imported?)
    :args->opts [:files]
    :spec       {:files {:coerce []}}}
   {:cmds       ["import" "workouts"]
    :fn         #(i/handle-import % s/workouts s/workouts-imported?)
    :args->opts [:files]
    :spec       {:files {:coerce []}}}
   {:cmds       ["export"]
    :fn         #(-> % e/prepare (e/export* (vec @s/foods)))
    :args->opts [:file]}
   {:cmds       ["new" "food"]
    :fn         (fn [{:keys [opts]}]
                  (let [food (n/new-food opts)]
                    (swap! s/foods conj food)))
    :spec       n/new-food-spec}
   {:cmds       ["new" "fluid"]
    :fn         (fn [{:keys [opts]}]
                  (let [fluid (n/new-fluid opts)]
                    (swap! s/fluids conj fluid)))
    :spec       n/new-fluid-spec}
   {:cmds       ["new" "log"]
    :fn         (fn [{:keys [opts]}]
                  (reset! s/log (n/new-log opts)))
    :spec       n/new-log-spec}
   {:cmds       ["log" "food"]
    :fn         (fn [{:keys [opts]}]
                  (let [[food & others] (l/log-food opts)]
                    (when-not others
                      (swap! s/log update :foods conj food))))
    :spec       l/log-food-spec}
   {:cmds       ["log" "fluid"]
    :fn         (fn [{:keys [opts]}]
                  (let [[fluid & others] (l/log-fluid opts)]
                    (when-not others
                      (swap! s/log update :fluids conj fluid))))
    :spec       l/log-fluid-spec}])

(defn make-log-fspec
  [date]
  (doto (->> (str date)
             (#(str/split % #"-"))
             (into ["logs"])
             (str/join fs/file-separator)
             (#(str % ".edn")))
    (#(-> % fs/parent fs/create-dirs))))

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
         log-fspec#      (fs/file ~(make-log-fspec date))]
     (try
       (i/handle-import foods-fspec# s/foods s/foods-imported?)
       (i/handle-import fluids-fspec# s/fluids s/fluids-imported?)
       (i/handle-import workouts-fspec# s/workouts s/workouts-imported?)

       (reset! s/log (i/maybe-import log-fspec#))

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

(defn -main
  [& args]
  (with-log {}
    (cli/dispatch commands args)))

(comment

  ;; Collect all logged foods and show the daily macros
  (->> (s/combine :foods s/foods-by-id a/add-macros)
       ((fn [{:keys [calories protein carbs fat]}]
          (format "%.1f %s :: (%.1f%s p, %.1f%s c, %.1f%s f)"
                  (u/amt calories) (name (u/units calories))
                  (u/amt protein)  (name (u/units protein))
                  (u/amt carbs)    (name (u/units carbs))
                  (u/amt fat)      (name (u/units fat)))))
       println)

  ::end)
