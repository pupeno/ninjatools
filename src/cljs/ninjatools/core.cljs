;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [ninjatools.handlers]
            [ninjatools.routing :as routing]
            [ninjatools.views :as views]
            ninjatools.auth
            ninjatools.tools
            ninjatools.human))

(defn mount-root []
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (routing/start!)
  (re-frame/dispatch-sync [:initialize-db])
  (mount-root))
