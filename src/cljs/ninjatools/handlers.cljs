;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.handlers
  (:require [re-frame.core :as re-frame]))

(re-frame/register-handler
  :initialize-db
  (fn [_ _]
    (re-frame/dispatch [:get-current-user])
    {:current-route     nil
     :alerts            (sorted-map)
     :current-user      nil
     :log-in-form       {}
     :registration-form {}
     :tools             {:by-id   {}
                         :by-slug {}}
     :tools-in-use      #{}}))

(defmulti display-page :name)
(defmethod display-page :default [_current-route db]
  db)

(re-frame/register-handler
  :set-current-route
  (fn [db [_name current-route]]
    (display-page current-route (assoc db :current-route current-route))))
