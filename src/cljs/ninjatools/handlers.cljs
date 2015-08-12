;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.handlers
  (:require [re-frame.core :as re-frame]
            [ninjatools.db :as db]
            [ajax.core :as ajax]))

(re-frame/register-handler
  :initialize-db
  (fn [_ _]
    {:tools {:data    {}
             :by-slug {}}}))

(re-frame/register-handler
  :display-page-about
  (fn [db [_ _]]
    (assoc db :active-panel :about-panel)))

(re-frame/register-handler
  :display-page-tools
  (fn [db [_ _]]
    (when (empty? (get-in db [:tools :data]))
      (re-frame/dispatch [:get-tools]))
    (assoc db :active-panel :tools-panel)))

(re-frame/register-handler
  :display-page-tool
  (fn [db [_ args]]
    (when (nil? (db/get-tool-by-slug db (:slug args)))
      (re-frame/dispatch [:get-tools]))
    (-> db
        (assoc :active-panel :tool-panel)
        (assoc :current-tool-slug (:slug args)))))

(re-frame/register-handler
  :got-tools
  (fn [db [_ tools]]                                        ;; TODO: remove loading page mark, once we started marking pages as loading.
    (assoc db :tools {:data    (reduce #(assoc %1 (%2 "id") %2) {} tools)
                      :by-slug (reduce #(assoc %1 (%2 "slug") (%2 "id")) {} tools)})))

(re-frame/register-handler
  :get-tools
  (fn [db [_]]
    (ajax/GET "/api/v1/tools"
              {:handler       #(re-frame/dispatch [:got-tools %1])
               :error-handler (fn [{:keys [status status-text]}]
                                (js/alert "We are sorry, there was an unexpected error.")
                                (.log js/console (pr-str "Error: " status status-text)))})
    db))                                                    ;; TODO: mark the page as *loading*