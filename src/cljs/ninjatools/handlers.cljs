;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.handlers
  (:require [re-frame.core :as re-frame]
            [ninjatools.db :as db]
            [ajax.core :as ajax]))

(re-frame/register-handler
  :initialize-db
  (fn [_ _]
    db/default-db))

(re-frame/register-handler
  :display-about-panel
  (fn [db [_]]
    (assoc db :active-panel :about-panel)))

(re-frame/register-handler
  :display-tools-panel
  (fn [db [_]]
    (when (empty? (get-in db [:tools :data]))
      (re-frame/dispatch [:get-tools]))
    (assoc db :active-panel :tools-panel)))

(re-frame/register-handler
  :display-tool-panel
  (fn [db [_ slug]]
    (when (nil? (db/get-tool-by-slug db slug))
      (re-frame/dispatch [:get-tools]))
    (-> db
        (assoc :active-panel :tool-panel)
        (assoc :current-tool-slug slug))))

(re-frame/register-handler
  :got-tools
  (fn [db [_ tools]]                                        ;; TODO: remove loading page mark, once we started marking pages as loading.
    (assoc db :tools {:data    (reduce #(assoc %1 (%2 "id") %2) {} tools)
                      :by-slug (reduce #(assoc %1 (%2 "slug") (%2 "id")) {} tools)})))

(re-frame/register-handler
  :get-tools
  (fn [db [_]]
    (ajax/GET "/tools"
              {:handler       #(re-frame/dispatch [:got-tools %1])
               :error-handler #(do
                                (js/alert "There was an unexpected error.")
                                (.log js/console (pr-str "Error" %1)))})
    db))                                                    ;; TODO: mark the page as *loading*