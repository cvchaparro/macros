(ns io.cvcf.macros.entrypoint
  (:require
   [babashka.cli :as cli]
   [io.cvcf.macros.arithmetic :as a]
   [io.cvcf.macros.export :as e]
   [io.cvcf.macros.import.core :as i]
   [io.cvcf.macros.import.csv]
   [io.cvcf.macros.import.edn]
   [io.cvcf.macros.log :as l]
   [io.cvcf.macros.new :as n]
   [io.cvcf.macros.store :as s]
   [io.cvcf.macros.utils :as u]))

(def commands
  [{:cmds       ["import"]
    :fn         #(->> %
                      i/prepare
                      (mapcat i/import*)
                      (reset! s/foods))
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

(defn -main
  [& args]
  (cli/dispatch commands args))

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

  ;; Update stats based on combined calories and macros
  (let [all (->> (:foods @s/log)
                 (map #(assoc (get (s/foods-by-id) ((comp str :id) %))
                              :n
                              (u/qty (:servings %) :serving)))
                 (reduce a/add-macros))]
    (swap! s/log update-in [:stats :calories] assoc :in (:calories all))
    (swap! s/log update-in [:stats] assoc :macros (select-keys all [:protein :carbs :fat])))

  ::end)
