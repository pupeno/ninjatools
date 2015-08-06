;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.routes
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.History)
  (:require [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [re-frame.core :as re-frame]))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      EventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn app-routes []
  (secretary/set-config! :prefix "#")
  ;; --------------------
  ;; define routes here
  (defroute "/" []
            (re-frame/dispatch [:display-tools-panel]))

  (defroute "/tool/:slug" [slug]
            (re-frame/dispatch [:display-tool-panel slug]))

  (defroute "/about" []
            (re-frame/dispatch [:set-active-panel :about-panel]))


  ;; --------------------
  (hook-browser-navigation!))
