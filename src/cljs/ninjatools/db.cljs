;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.db
  (:require [re-frame.core :as re-frame]))

(def initial-db {:current-route        nil
                 :alerts               (sorted-map)
                 :current-user         nil
                 :log-in-form          {}
                 :registration-form    {}
                 :reset-password-form  {}
                 :change-password-form {}
                 :tools                nil
                 :used-tools           nil})

(re-frame/register-handler
  :initialize-db
  (fn [_ _]
    (re-frame/dispatch [:get-current-user])
    initial-db))
