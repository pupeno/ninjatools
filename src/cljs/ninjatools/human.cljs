;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.human
  (:require [re-frame.core :as re-frame]))

(defn dispatch [& args]
  (re-frame/dispatch [:human-interaction])
  (apply re-frame/dispatch args))

; TODO: implement a way for many functions to be called when human-interaction is dispatched. Right now we don't need  this as we have only one event handler in alerts.cljs.
