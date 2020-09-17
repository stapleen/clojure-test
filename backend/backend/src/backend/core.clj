(ns backend.core
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.java.jdbc :as jdbc]
            [backend.config :as config]
            [ring.middleware.json :as middleware]
            [ring.util.response :refer [response]]))

(def db config/db-config)

(defn add-patients
  [request]
  (let [body (get-in request [:body])
        full-name (get-in request [:body "full_name"])
        gender (get-in request [:body "gender"])
        date-of-birth (get-in request [:body "date_of_birth"])
        date-of-birth-convert-date (java.sql.Date/valueOf date-of-birth)
        current-date (.getTime (java.util.Date.))
        current-date-convert-date (-> current-date java.sql.Timestamp. .toLocalDateTime)]

    (jdbc/insert! db :patients {:full_name full-name
                                :gender gender
                                :date_of_birth date-of-birth-convert-date
                                :created_at current-date-convert-date})
    (response {:success 1})))
   
(defn get-patients [request]
(let [patients-list (jdbc/query db ["SELECT id, full_name, gender, date_of_birth FROM patients WHERE deleted=false"])]
 (response {:success 1 :result patients-list})))

(defn delete-patient [request]
    (let [body (get-in request [:body])
          patient-id (get-in request [:body "id"])
          current-date (.getTime (java.util.Date.))
          current-date-convert-date (-> current-date java.sql.Timestamp. .toLocalDateTime)]

      (jdbc/update! db :patients {:deleted true :updated_at current-date-convert-date} ["id = ?" patient-id])
      (response {:success 1})))

(defn update-patient-data [request]
  (let [body (get-in request [:body])
        id (get-in request [:body "id"])
        full-name (get-in request [:body "full_name"])
        gender (get-in request [:body "gender"])
        date-of-birth (get-in request [:body "date_of_birth"])
        date-of-birth-convert-date (if (nil? date-of-birth) nil (java.sql.Date/valueOf date-of-birth))
        current-date (.getTime (java.util.Date.))
        current-date-convert-date (-> current-date java.sql.Timestamp. .toLocalDateTime)
        ]

    (jdbc/execute! db
                   ["UPDATE patients SET full_name = COALESCE(?, full_name),
                   gender = COALESCE(?, gender),
                   date_of_birth = COALESCE(?, date_of_birth),
                   updated_at = ?
                   WHERE id = ?" full-name gender date-of-birth-convert-date current-date-convert-date id])

    (response {:success 1})))

(defroutes app
  (POST "/add" [] (-> add-patients middleware/wrap-json-body middleware/wrap-json-response))
  (GET "/get" [] (middleware/wrap-json-response get-patients))
  (DELETE "/delete" [] (-> delete-patient middleware/wrap-json-body middleware/wrap-json-response))
  (PATCH "/update" [] (-> update-patient-data middleware/wrap-json-body middleware/wrap-json-response)))

(defn -main []
  (jetty/run-jetty app {:port 3000}))