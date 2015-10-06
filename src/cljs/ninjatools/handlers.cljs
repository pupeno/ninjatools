;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.handlers
  (:require [re-frame.core :as re-frame]
            [ninjatools.db :as db]
            [ajax.core :as ajax]
            [ninjatools.util :refer [log]]
            [clojure.walk]))

(defn report-unexpected-error [{:keys [status status-text]}]
  (js/alert "We are sorry, there was an unexpected error.")
  (log "Error: " status status-text))

(re-frame/register-handler
  :initialize-db
  (fn [_ _]
    {:tools         {:by-id   {}
                     :by-slug {}}
     :current-route nil
     :tools-in-use  #{}}))

(defmulti display-page :name)

(defmethod display-page :default [_current-route db]
  db)

(defmethod display-page :home [_current-route db]
  (when (empty? (get-in db [:tools :by-id]))
    (re-frame/dispatch [:get-tools]))
  db)

(defmethod display-page :tools [_current-route db]
  (when (empty? (get-in db [:tools :by-id]))
    (re-frame/dispatch [:get-tools]))
  db)

(defmethod display-page :tool [current-route db]
  (re-frame/dispatch [:get-tool-with-integrations (:slug current-route)])
  (assoc db :current-tool-slug (:slug current-route)))

(re-frame/register-handler
  :set-current-route
  (fn [db [_name current-route]]
    (display-page current-route (assoc db :current-route current-route))))

(re-frame/register-handler
  :get-tools
  (fn [db [_]]
    (ajax/GET "/api/v1/tools"
              {:handler       #(re-frame/dispatch [:got-tools %1])
               :error-handler report-unexpected-error})
    db))

(re-frame/register-handler
  :got-tools
  (fn [db [_ tools]]
    (let [tools (map clojure.walk/keywordize-keys tools)]
      (assoc db :tools {:by-id   (reduce #(assoc %1 (:id %2) %2) {} tools)
                        :by-slug (reduce #(assoc %1 (:slug %2) %2) {} tools)}))))

(re-frame/register-handler
  :get-tool-with-integrations
  (fn [db [_ tool-slug tool-requested]]
    (if-let [tool (gen-in db [:tools :by-slug tool-slug])]
      (when (empty? (:integration-ids tool))
        (ajax/GET (str "/api/v1/tools/" (:id tool) "/integrations")
                  {:handler       #(re-frame/dispatch [:got-integrations (:id tool) %1])
                   :error-handler report-unexpected-error}))
      (do (when (not tool-requested)
            (re-frame/dispatch [:get-tools]))               ; TODO: only get the tool we want, by slug.
          (re-frame/dispatch [:get-tool-with-integrations tool-slug true])))
    db))

(re-frame/register-handler
  :got-integrations
  (fn [db [_ tool-id integration-ids]]
    (let [tool (assoc (get-in db [:tools :by-id tool-id]) :integration-ids integration-ids)]
      (assoc-in db [:tools :by-id tool-id] tool))))         ; TODO: get the tools that we have integration ids for when we stop getting all the tools all the time.

(re-frame/register-handler
  :mark-tool-as-used
  (fn [db [_ tool-id]]
    (update-in db [:tools-in-use] conj tool-id)))
