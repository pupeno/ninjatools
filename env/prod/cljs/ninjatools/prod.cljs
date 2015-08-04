;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.app
  (:require [ninjatools.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init)
