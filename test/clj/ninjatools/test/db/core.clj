;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.test.db.core
  (:require [ninjatools.db.core :as db]
            [conman.core :as conman]
            [ninjatools.db.migrations :as migrations]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [conman.core :refer [with-transaction]]
            [environ.core :refer [env]]))

(defn set-up-database-test []
  (use-fixtures :once (fn [f]
                        (db/connect!)
                        (migrations/migrate ["migrate"])
                        (f)))
  (use-fixtures :each (fn [test-function]
                        (conman/with-transaction
                          [t-conn db/conn]
                          (jdbc/db-set-rollback-only! t-conn)
                          (test-function)))))

(set-up-database-test)

(deftest test-users
  (is (= 1 (db/create-user!
             {:id         "1"
              :first-name "Sam"
              :last-name  "Smith"
              :email      "sam.smith@example.com"
              :pass       "pass"})))
  (is (= [{:id         "1"
           :first_name "Sam"
           :last_name  "Smith"
           :email      "sam.smith@example.com"
           :pass       "pass"
           :admin      nil
           :last_login nil
           :is_active  nil}]
         (db/get-user {:id "1"}))))
