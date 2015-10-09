;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns re-forms.re-frame
  (:require [re-forms.core :as core]
            [re-frame.core :as re-frame]))

(defn activate [values errors event form]
  (core/activate values
                 errors
                 (fn [ks value]
                   (let [event-v (if (fn? event)
                                   (event ks value)
                                   [event ks value])]
                     (re-frame/dispatch event-v)))
                 form))
