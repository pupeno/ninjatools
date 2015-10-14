;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.models.user
  (:require [schema.core :as s]
            [ninjatools.models.user-schema :as user-schema]
            [validateur.validation :as validateur]
            [ninjatools.db.core :as db]
            [buddy.hashers :as hashers]))

(defn create [user]
  (db/create-user<! (assoc user :password (hashers/encrypt (:password user)))))

(def registration-validation
  (validateur/compose-sets
    user-schema/registration-validation
    (validateur/validation-set
      (validateur/validate-with-predicate :email (fn [{email :email}] (= 0 (count (db/get-user-by-email email))))
                                          :message "Email address already taken"))))

(defn get-by-credentials [credentials]
  (when-let [user (db/get-user-by-email (:email credentials))]
    (when (hashers/check (:password credentials) (:password user))
      user)))

(defn sanitize-for-public [user]
  (select-keys user [:email :name]))
