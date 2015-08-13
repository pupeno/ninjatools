;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.util)

(defn log [& args]
  (.log js/console (pr-str args)))
