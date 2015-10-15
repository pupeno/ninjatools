;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.util)

(defn println-ret [val]
  (println val)
  val)

#?(:cljs
   (defn log [& args]
     (.log js/console (pr-str args))))
