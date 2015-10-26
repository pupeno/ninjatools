;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [ninjatools.routing :as routing]
            [ninjatools.layout :as layout]
            ninjatools.auth
            ninjatools.alerts
            ninjatools.tools
            ninjatools.ui
            ninjatools.pages))

(defn mount-root []
  (reagent/render [layout/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (routing/start!)
  (re-frame/dispatch-sync [:initialize-db])
  (mount-root))

(re-frame/register-handler
  :initialize-db
  (fn [_ _]
    (re-frame/dispatch [:get-current-user])
    {:current-route        nil
     :alerts               (sorted-map)
     :current-user         nil
     :log-in-form          {}
     :registration-form    {}
     :reset-password-form  {}
     :change-password-form {}
     :tools                nil
     :used-tools           nil}))
