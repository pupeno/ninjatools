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
  :set-active-panel
  (fn [db [_ active-panel]]
    (assoc db :active-panel active-panel)))

(re-frame/register-handler
  :display-tools-panel
  (fn [db [_ active-panel]]
    (when (not (contains? db :tools))
      (re-frame/dispatch [:get-tools]))
    (assoc db :active-panel :tools-panel)))

(re-frame/register-handler
  :display-tool-panel
  (fn [db [_ slug]]
    (when (not (contains? db :tools))
      (re-frame/dispatch [:get-tools]))
    (-> db
        (assoc :active-panel :tool-panel)
        (assoc :current-tool-slug slug))))

(re-frame/register-handler
  :got-tools
  (fn [db [_ tools]]                                        ;; TODO: remove loading page mark, once we started marking pages as loading.
    (assoc db :tools {:all     (reduce #(assoc %1 (%2 "id") %2) {} tools)
                      :by-slug (reduce #(assoc %1 (%2 "slug") (%2 "id")) {} tools)})))

(re-frame/register-handler
  :get-tools
  (fn [db [_]]
    (ajax/GET "/api/tools"
              {:handler       #(re-frame/dispatch [:got-tools %1])
               :error-handler #(.log js/console (pr-str "Error" %1))})
    db))                                                    ;; TODO: mark the page as *loading*