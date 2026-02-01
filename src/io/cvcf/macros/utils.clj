(ns io.cvcf.macros.utils
  (:require
   [babashka.fs :as fs]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [tick.core :as t]))

(defn ->double
  [x]
  (cond
    (string? x)  (parse-double x)
    (keyword? x) (parse-double (name x))
    (number? x)  (double x)
    :else        x))

(defn ->keyword
  [x]
  (cond
    (string? x) (-> x (str/replace #" " "-") keyword)
    (symbol? x) (keyword x)
    :else       x))

(defn valid-duration?
  [s]
  (->> (clojure.string/split s #":")
       (zipmap [:h :m])
       (map #(when-let [[k v] %]
               (and (= v (re-find #"[0-9]{1,2}" v))
                    [k (parse-long v)])))
       (map #(when-let [[k v] %]
               (cond (and (= k :h) (<= 10 v 23)) (str v)
                     (and (= k :m) (<= 10 v 59)) (str v)
                     (<= 0 v 9)                  (str "0" v))))
       (take-while identity)
       (#(and (= 2 (count %))
              (str/join ":" %)))))

(defn ->duration
  [x]
  (if-let [d (valid-duration? x)]
    (t/between
     (-> (t/date) (t/at "00:00"))
     (-> (t/date) (t/at d)))
    (println (str "Error parsing duration '" x "'"))))

(defn qty
  [amount units]
  {:amount (->double amount) :units (->keyword units)})

(defn amt   [{:keys [amount]}] amount)
(defn units [{:keys [units]}]  units)

(defn replace-value
  [m k match replacement]
  (assoc m k (str/replace (k m) match replacement)))

(defn new-resource
  [f]
  (let [r (or (io/resource f) (fs/file "resources" f))]
    (when-not (fs/exists? r) (spit r "[]"))
    r))

(defn ensure-file-exists
  [f]
  (when-not (fs/exists? f)
    (println "File does not exist. Creating:" f)
    (-> f fs/parent fs/create-dirs)
    f))
