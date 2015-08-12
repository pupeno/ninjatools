;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              [ninjatools.handlers]
              [ninjatools.subs]
              [ninjatools.routes :as routes]
              [ninjatools.views :as views]))

(defn mount-root []
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init [] 
  (routes/start!)
  (re-frame/dispatch-sync [:initialize-db])
  (mount-root))
