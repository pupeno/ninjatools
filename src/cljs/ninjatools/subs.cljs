;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/register-sub
 :active-panel
 (fn [db _]
   (reaction (:active-panel @db))))

(re-frame/register-sub
  :tools
  (fn [db _]
    (reaction (:tools @db))))