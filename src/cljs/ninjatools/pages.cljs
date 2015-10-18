;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.pages
  (:require [ninjatools.layout :as layout]))

(defn about-page []
  (fn []
    [:div "This is the About Page."]))

(defmethod layout/pages :about [] [about-page])
