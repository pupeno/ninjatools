;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.models.user-schema
  (:require [schema.core :as s]
            [validateur.validation :as validateur]))


(defn validation-errors-schema [schema]
  (reduce (fn [new-schema [key _]] (assoc new-schema (s/optional-key key) #{s/Str}))
          {}
          schema))

(s/defschema LogInSchema {:email    s/Str
                          :password s/Str})

(s/defschema LogInValidationSchema
  (assoc LogInSchema
    (s/optional-key :errors) (validation-errors-schema LogInSchema)))

(def log-in-validation
  (validateur/validation-set
    (validateur/presence-of :email :message "Email address can't be blank.")
    (validateur/format-of :email :format #".+@.+\..+" :allow-blank true :allow-nil true
                          :message-fn (fn [& _] "Email address doesn't look valid."))
    (validateur/length-of :email :within (range -1 255) :allow-blank true :allow-nil true
                          :message-fn (fn [& _] "Email address can't be longer than 254 characters."))
    (validateur/presence-of :password :message "Password can't be blank.")))

(s/defschema RegistrationSchema {:email                 s/Str
                                 :password              s/Str
                                 :password-confirmation s/Str})

(s/defschema RegistrationValidationSchema
  (assoc RegistrationSchema
    (s/optional-key :errors) (validation-errors-schema RegistrationSchema)))

(def registration-validation
  (validateur/validation-set
    (validateur/presence-of :email :message "Email address can't be blank.")
    (validateur/format-of :email :format #".+@.+\..+" :allow-blank true :allow-nil true
                          :message-fn (fn [& _] "Email address doesn't look valid."))
    (validateur/length-of :email :within (range -1 255) :allow-blank true :allow-nil true
                          :message-fn (fn [& _] "Email address can't be longer than 254 characters."))
    (validateur/presence-of :password :message "Password can't be blank.")
    (validateur/length-of :password :within (range 8 255) :allow-blank true :allow-nil true
                          :message-fn (fn [& _] "Password can't be shorter than 8 characters."))
    (validateur/validate-with-predicate :password-confirmation
                                        (fn [values]
                                          (= (:password values) (:password-confirmation values)))
                                        :message "Password confirmation doesn't match password.")))

(s/defschema ResetPasswordSchema {:email s/Str})

(s/defschema ResetPasswordValidationSchema
  (assoc RegistrationSchema
    (s/optional-key :errors) (validation-errors-schema RegistrationSchema)))

(def reset-password-validation
  (validateur/validation-set
    (validateur/presence-of :email :message "Email address can't be blank.")
    (validateur/format-of :email :format #".+@.+\..+" :allow-blank true :allow-nil true
                          :message-fn (fn [& _] "Email address doesn't look valid."))
    (validateur/length-of :email :within (range -1 255) :allow-blank true :allow-nil true
                          :message-fn (fn [& _] "Email address can't be longer than 254 characters."))))

(s/defschema ChangePasswordSchema {:password              s/Str
                                   :password-confirmation s/Str
                                   :token                 s/Uuid})

(s/defschema ChangePasswordValidationSchema
  (assoc ChangePasswordSchema
    (s/optional-key :errors) (validation-errors-schema ChangePasswordSchema)))

(def change-password-validation
  (validateur/validation-set
    (validateur/presence-of :password :message "Password can't be blank.")
    (validateur/length-of :password :within (range 8 255) :allow-blank true :allow-nil true
                          :message-fn (fn [& _] "Password can't be shorter than 8 characters."))
    (validateur/validate-with-predicate :password-confirmation
                                        (fn [values]
                                          (= (:password values) (:password-confirmation values)))
                                        :message "Password confirmation doesn't match password.")))

(defn display-name [user]
  (or (:name user)
      (first (clojure.string/split (:email user) #"@"))))
