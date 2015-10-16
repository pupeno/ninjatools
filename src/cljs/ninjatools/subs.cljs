;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]))

(re-frame/register-sub
  :current-route
  (fn [db _]
    (reaction (:current-route @db))))
