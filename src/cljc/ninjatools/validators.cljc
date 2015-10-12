;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.validators
  (:require [validateur.validation :as validateur]))


(def registration
  (validateur/validation-set
    (validateur/format-of :email :format #".+@.+\..+" :message-fn (fn [_] "Email can't be blank or invalid."))
    (validateur/length-of :password :within (range 8 255)
                          :message-fn (fn [_] "Password can't be blank or shorter than 8 characters."))
    (validateur/validate-with-predicate :password-confirmation
                                        (fn [values]
                                          (= (:password values) (:password-confirmation values)))
                                        :message "Password confirmation doesn't match password.")))


