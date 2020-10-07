CREATE DATABASE clojure_test;

 \c clojure_test 

CREATE TABLE patients (
  id SERIAL NOT NULL PRIMARY KEY,
  full_name VARCHAR(50) NOT NULL,
  gender VARCHAR(1) NOT NULL,
  date_of_birth DATE NOT NULL,
  deleted BOOL NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ,
  updated_at TIMESTAMPTZ  
);