(ns frontend.components.patients
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [reagent.core :as r]
   [cljs-http.client :as http]
   [cljs.core.async :refer [<!]]
   [moment :as moment]
   [reagent-material-ui.core.circular-progress :refer [circular-progress]]
   [frontend.config :as config]
   [reagent-material-ui.core.table :refer [table]]
   [reagent-material-ui.core.table-body :refer [table-body]]
   [reagent-material-ui.core.table-cell :refer [table-cell]]
   [reagent-material-ui.core.table-container :refer [table-container]]
   [reagent-material-ui.core.table-head :refer [table-head]]
   [reagent-material-ui.core.table-row :refer [table-row]]
   [reagent-material-ui.styles :as styles]
   [reagent-material-ui.core.paper :refer [paper]]))

(defn component
  []
  (let [patients (r/atom nil)
        loading? (r/atom true)]

    (r/create-class
     {:display-name  "patients"

      :component-did-mount
      (fn [this]
        (go (let [response (<! (http/get (str config/url "/get")))
                  result (get-in response [:body :result])]
              (reset! patients result)
              (reset! loading? false))))

      :reagent-render
      (fn []
        (let [patients-list
              (map #(let [id (get-in % [:id])
                          full-name (get-in % [:full_name])
                          gender (get-in % [:gender])
                          date_of_birth (get-in % [:date_of_birth])
                          date-formated (.format  (moment. date_of_birth) "DD.MM.YYYY")]
                      [table-row {:key id}
                       [table-cell full-name]
                       [table-cell gender]
                       [table-cell date-formated]
                       [table-cell
                        [:div
                         [:input {:type "button"
                                  :value "Редактировать"
                                  :on-click (fn []
                                              (set! (.. js/document -location -href) (str "#/edit/" id)))}]]
                        [:input {:type "button"
                                 :value "Удалить"
                                 :on-click
                                 (fn []
                                   (go (let [response (<! (http/post (str config/url "/delete")
                                                                     {:json-params {:id id}}))
                                             success (get-in response [:body :success])]
                                         (if (zero? success)
                                           (js/alert (get-in response [:body :error]))
                                           (reset! patients
                                                   (filterv
                                                    (fn [x] (not= (get-in x [:id]) id)) @patients))))))}]]])
                   @patients)]

          (if (true? @loading?) [circular-progress {:color "secondary"}]
              (if (empty? patients-list)
                [:div
                 [:input {:type "button"
                          :value "Добавить пациента"
                          :on-click (fn [] (set! (.. js/document -location -href) "#/new"))}]
                 [:p "Список пациентов пуст"]]
                [paper
                 [:input {:type "button"
                          :value "Добавить пациента"
                          :on-click (fn [] (set! (.. js/document -location -href) "#/new"))}]
                 [table-container
                  [table
                   [table-head
                    [table-row
                     [table-cell "Полное имя"]
                     [table-cell "Пол"]
                     [table-cell "Дата рождения"]
                     [table-cell ""]]]
                   [table-body
                    patients-list]]]]))))})))