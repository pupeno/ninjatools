;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]
            [ninjatools.db :as db]))

(re-frame/register-sub
  :current-route
  (fn [db _]
    (reaction (:current-route @db))))

(re-frame/register-sub
  :active-panel
  (fn [db _]
    (reaction (:active-panel @db))))

(re-frame/register-sub
  :tools
  (fn [db _]
    (reaction (:tools @db))))

(re-frame/register-sub
  :current-tool
  (fn [db _]
    (reaction (db/get-tool-by-slug @db (get @db :current-tool-slug)))))
