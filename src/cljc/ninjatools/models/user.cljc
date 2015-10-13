;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.models.user
  (:require [schema.core :as s]
            [validateur.validation :as validateur]))

(s/defschema RegistrationSchema {:email                 s/Str
                                 :password              s/Str
                                 :password-confirmation s/Str})

(defn validation-errors-schema [schema]
  (reduce (fn [new-schema [key _]] (assoc new-schema (s/optional-key key) #{s/Str}))
          {}
          schema))

(s/defschema RegistrationValidationSchema
  (assoc RegistrationSchema
    (s/optional-key :validation-errors) (validation-errors-schema RegistrationSchema)))

(def registration-validation
  (validateur/validation-set
    (validateur/format-of :email :format #".+@.+\..+" :message-fn (fn [& _] "Email can't be blank or invalid."))
    #?(:clj (validateur/validate-with-predicate :email (fn [values]
                                                         (println "Simulating" (:email values) "already taken.")
                                                         false)
                                                :message "Email address already taken"))
    (validateur/length-of :password :within (range 8 255)
                          :message-fn (fn [& _] "Password can't be blank or shorter than 8 characters."))
    (validateur/validate-with-predicate :password-confirmation
                                        (fn [values]
                                          (= (:password values) (:password-confirmation values)))
                                        :message "Password confirmation doesn't match password.")))

#?(:clj
   (defn create [registration]
     (if (validateur/valid? registration-validation registration)
       {:status :success :registration registration}
       {:status :validation-error :registration (assoc registration :validation-errors (registration-validation registration))})))
