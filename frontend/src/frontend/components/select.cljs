(ns frontend.components.select)

(defn component
  [label value func]
  (println "value" value)
  [:div
   [:span (str label)]
   [:select {:on-change func}
    [:option {:value "М" :selected (= value "М")} "М"]
    [:option {:value "Ж" :selected (= value "Ж")} "Ж"]]])