;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.test.db.core
  (:require [ninjatools.db.core :as db]
            [ninjatools.db.migrations :as migrations]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [environ.core :refer [env]]))

(use-fixtures
  :once
  (fn [f]
    (db/connect!)
    (migrations/migrate ["migrate"])
    (f)))

(deftest test-users
  (db/with-transaction t-conn
                       (jdbc/db-set-rollback-only! t-conn)
                       (is (= 1 (db/create-user!
                                  {:id         "1"
                                   :first_name "Sam"
                                   :last_name  "Smith"
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
                              (db/get-user {:id "1"})))))
