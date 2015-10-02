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
  :tools
  (fn [db _]
    (reaction (:tools @db))))

(re-frame/register-sub
  :tools-in-use
  (fn [db _]
    (reaction (:tools-in-use @db))))

(re-frame/register-sub
  :current-tool
  (fn [db _]
    (reaction (db/get-tool-by-slug @db (get @db :current-tool-slug)))))

(re-frame/register-sub
  :current-available-tools
  (fn [db _]
    (let [page-number (if-let [raw-page-number ((:query (:url (:current-route @db))) "p")]
                        (js/parseInt raw-page-number)
                        0)]
      (reaction {:tools       (doall (take 10 (drop (* 10 page-number)
                                                    (filter #(not (contains? (:tools-in-use @db) (:id %)))
                                                            (vals (:data (:tools @db)))))))
                 :page-number page-number}))))
