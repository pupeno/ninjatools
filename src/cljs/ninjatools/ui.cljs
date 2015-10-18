;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.ui
  (:require [re-frame.core :as re-frame]))

(defn dispatch [event & args]
  (.preventDefault event)
  (re-frame/dispatch [:ui-interaction])
  (apply re-frame/dispatch args))

; TODO: implement a way for many functions to be called when ui-interaction is dispatched. Right now we don't need  this as we have only one event handler in alerts.cljs.

(defn loading
  "Display a loading panel"
  []
  [:div "Loading..."])
