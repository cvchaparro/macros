(ns io.cvcf.macros.entrypoint
  (:require
   [babashka.cli :as cli]
   [babashka.fs :as fs]
   [clojure.java.io :as io]
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
    :fn         #(do (reset! s/foods (i/maybe-import %))
                     (reset! s/foods-imported? true))
    :args->opts [:files]
    :spec       {:files {:coerce []}}}
   {:cmds       ["import" "fluids"]
    :fn         #(do (reset! s/fluids (i/maybe-import %))
                     (reset! s/fluids-imported? true))
    :args->opts [:files]
    :spec       {:files {:coerce []}}}
   {:cmds       ["export"]
    :fn         #(-> %
                     e/prepare
                     (e/export* (vec @s/foods)))
    :args->opts [:file]}
   {:cmds ["new" "food"]
    :fn   (fn [{:keys [opts]}]
            (let [food (n/new-food opts)]
              (swap! s/foods conj food)))
    :spec n/new-food-spec}
   {:cmds ["new" "fluid"]
    :fn   (fn [{:keys [opts]}]
            (let [fluid (n/new-fluid opts)]
              (swap! s/fluids conj fluid)))
    :spec n/new-fluid-spec}
   {:cmds ["new" "log"]
    :fn   (fn [{:keys [opts]}]
            (reset! s/log (n/new-log opts)))
    :spec n/new-log-spec}
   {:cmds ["log" "food"]
    :fn   (fn [{:keys [opts]}]
            (let [[food & others] (l/log-food opts)]
              (when-not others
                (swap! s/log update :foods conj food))))
    :spec l/log-food-spec}])

(defn make-log-fspec
  [date]
  (doto (->> (str date)
             (#(str/split % #"-"))
             (into ["logs"])
             (str/join fs/file-separator)
             (#(str % ".edn")))
    (#(-> % fs/parent fs/create-dirs))))

(defmacro with-log
  [{:keys [foods-fspec fluids-fspec date] :or {foods-fspec s/*foods-file* fluids-fspec s/*fluids-file* date (t/today)}} & body]
  `(let [foods-fspec#  (io/resource ~foods-fspec)
         fluids-fspec# (io/resource ~fluids-fspec)
         log-fspec#    (fs/file ~(make-log-fspec date))]
     (try
       (reset! s/foods  (i/maybe-import foods-fspec#))
       (reset! s/fluids (i/maybe-import fluids-fspec#))
       (reset! s/log    (i/maybe-import log-fspec#))

       ~@body

       (catch Exception e#
         (println e#))
       (finally
         (when (deref s/foods-changed?)
           (e/export* foods-fspec# (deref s/foods)))

         (when (deref s/fluids-changed?)
           (e/export* fluids-fspec# (deref s/fluids)))

         (s/update-stats)
         (e/export* log-fspec# (deref s/log))))))

(defn -main
  [& args]
  (with-log {}
    (cli/dispatch commands args)))

(comment

  ;; Collect all logged foods and show the daily macros
  (->> (:foods @s/log)
       (map #(assoc (get (s/foods-by-id) ((comp str :id) %))
                    :n
                    (u/qty (:servings %) :serving)))
       (reduce a/add-macros)
       ((fn [{:keys [calories protein carbs fat]}]
          (format "%.1f %s :: (%.1f%s p, %.1f%s c, %.1f%s f)"
                  (u/amt calories) (name (u/units calories))
                  (u/amt protein)  (name (u/units protein))
                  (u/amt carbs)    (name (u/units carbs))
                  (u/amt fat)      (name (u/units fat)))))
       println)

  ::end)
