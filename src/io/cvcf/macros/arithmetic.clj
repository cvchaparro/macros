(ns io.cvcf.macros.arithmetic
  (:require
   [io.cvcf.macros.utils :as u]))

(def multiplicative-identity (u/qty 1 :unit))
(def additive-identity       (u/qty 0 :unit))

(defn times
  ([]           multiplicative-identity)
  ([x]          (u/qty (u/amt x) (u/units x)))
  ([x y]        (u/qty (* (u/amt x) (u/amt y)) (u/units y)))
  ([x y & more] (reduce times (into [x y] more))))

(defn plus
  ([]           additive-identity)
  ([x]          (u/qty (u/amt x) (u/units x)))
  ([x y]        (u/qty (+ (u/amt x) (u/amt y)) (u/units y)))
  ([x y & more] (reduce plus (into [x y] more))))

(defn minus
  ([x]          (u/qty (u/amt (- x)) (u/units x)))
  ([x y]        (u/qty (- (u/amt x) (u/amt y)) (u/units y)))
  ([x y & more] (reduce minus (into [x y] more))))

(defn create-identity
  [& {:keys [mks aks]}]
  (->> [(map #(into {% multiplicative-identity}) mks)
        (map #(into {% additive-identity}) aks)]
       (apply concat)
       (into {})))

(defn add-macros
  ([f]
   (add-macros
    (create-identity :mks [:calories :protein :carbs :fat] :aks [:n])
    f))
  ([{n1 :n cal1 :calories p1 :protein c1 :carbs f1 :fat}
    {n2 :n cal2 :calories p2 :protein c2 :carbs f2 :fat}]
   {:n        (u/qty 1 :serving)
    :calories (plus (times n1 cal1) (times n2 cal2))
    :protein  (plus (times n1 p1)   (times n2 p2))
    :carbs    (plus (times n1 c1)   (times n2 c2))
    :fat      (plus (times n1 f1)   (times n2 f2))}))

(defn add-fluids
  ([f]
   (add-fluids (create-identity :mks [:n] :aks [:servings]) f))
  ([{n1 :n s1 :servings} {n2 :n s2 :servings}]
   {:n        (u/qty 1 :serving)
    :servings (plus (times n1 s1) (times n2 s2))}))
