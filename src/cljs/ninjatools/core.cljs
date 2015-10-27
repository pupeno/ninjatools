;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [ninjatools.routing :as routing]
            [ninjatools.layout :as layout]
            ninjatools.db
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
