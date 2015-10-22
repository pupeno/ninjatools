;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.pages
  (:require [re-frame.core :as re-frame]
            [ajax.core :as ajax]
            [ninjatools.layout :as layout]
            [ninjatools.util :as util]))

(defn about-page []
  (fn []
    [:div "This is the About Page."]))

(defmethod layout/pages :about [] [about-page])

(re-frame/register-handler
  :fail
  (fn [db [_]]
    (ajax/GET "/api/v1/fail" {:error-handler util/report-unexpected-error})
    db))

(defn fail-page []
  (fn []
    [:div
     [:h1 "Page to test error reporting"]
     [:p [:a.btn.btn-default {:on-click #(throw (js/Error "Bogus error to test exception handling on the client"))} "Client Fail!"]]
     [:p [:a.btn.btn-default {:href "/server-fail"} "Non-API Server Fail!"]]
     [:p [:a.btn.btn-default {:on-click #(re-frame/dispatch [:fail])} "API Server Fail!"]]]))

(defmethod layout/pages :fail [] [fail-page])
