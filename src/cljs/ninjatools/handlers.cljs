;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.handlers
  (:require [re-frame.core :as re-frame]
            [ninjatools.db :as db]
            [ajax.core :as ajax]
            [ninjatools.util :refer [log]]
            [clojure.walk]
            [validateur.validation :as validateur]
            [ninjatools.models.user-schema :as user-schema]))

(defn report-unexpected-error [{:keys [status status-text]}]
  (js/alert "We are sorry, there was an unexpected error.")
  (log "Error: " status status-text))

(re-frame/register-handler
  :initialize-db
  (fn [_ _]
    (re-frame/dispatch [:get-current-user])
    {:current-route     nil
     :alerts            (sorted-map)
     :current-user      nil
     :log-in-form       {}
     :registration-form {}
     :tools             {:by-id   {}
                         :by-slug {}}
     :tools-in-use      #{}}))

(defmulti display-page :name)
(defmethod display-page :default [_current-route db]
  db)

(re-frame/register-handler
  :set-current-route
  (fn [db [_name current-route]]
    (display-page current-route (assoc db :current-route current-route))))

(re-frame/register-handler
  :remove-alert
  (fn [db [_ id]]
    (db/remove-alert db id)))

(re-frame/register-handler
  :human-interaction
  (fn [db & _]
    (db/remove-old-alerts db)))
