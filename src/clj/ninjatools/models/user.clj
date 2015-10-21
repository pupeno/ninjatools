;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.models.user
  (:require [schema.core :as s]
            [ninjatools.models.user-schema :as user-schema]
            [validateur.validation :as validateur]
            [ninjatools.db.core :as db]
            [buddy.hashers :as hashers]
            [ninjatools.util :refer [println-ret]]))

(defn- encrypt-password [password]
  (hashers/encrypt password))

(defn check-password [user password]
  (hashers/check password (:password user)))

(defn create [user]
  (db/create-user<! (assoc user :password (encrypt-password (:password user)))))

(defn update-password [user new-password]
  (db/update-password<! {:id       (:id user)
                         :password (encrypt-password new-password)}))

(def registration-validation
  (validateur/compose-sets
    user-schema/registration-validation
    (validateur/validation-set
      (validateur/validate-with-predicate :email (fn [{email :email}] (= 0 (count (db/get-user-by-email email))))
                                          :message "Email address already taken"))))

(defn get-by-credentials [credentials]
  (when-let [user (db/get-user-by-email (:email credentials))]
    (when (check-password user (:password credentials))
      user)))

(defn sanitize-for-public [user]
  (select-keys user [:email :name]))
