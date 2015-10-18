;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [ninjatools.routing :as routing]
            [ninjatools.views :as views]
            ninjatools.auth
            ninjatools.alerts
            ninjatools.tools
            ninjatools.ui))

(defn mount-root []
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (routing/start!)
  (re-frame/dispatch-sync [:initialize-db])
  (mount-root))

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
