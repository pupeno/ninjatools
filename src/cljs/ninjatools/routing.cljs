;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.routing
  (:require [clojure.set :refer [rename-keys]]
            [domkm.silk :as silk]
            [pushy.core :as pushy]
            [reagent.ratom :as ratom :include-macros true]
            [re-frame.core :as re-frame]))

(def routes (silk/routes [[:home [[]]]
                          [:tools [["tools"]]]
                          [:tool [["tool" :slug]]]
                          [:about [["about"]]]
                          [:register [["register"]]]
                          [:log-in [["login"]]]
                          [:reset-password [["reset-password"]]]
                          [:change-password [["change-password"]]]
                          [:fail [["fail"]]]]))

(defn sanitize-silk-keywords [matched-route]
  (rename-keys matched-route {:domkm.silk/name    :name
                              :domkm.silk/pattern :pattern
                              :domkm.silk/routes  :routes
                              :domkm.silk/url     :url}))

(defn parse-path [path]
  (sanitize-silk-keywords (silk/arrive routes path)))

(defn routing-event [matched-route]
  [:set-current-route matched-route])

(defn dispatch-route [parsed-path]
  (re-frame/dispatch [:ui-interaction])
  (re-frame/dispatch (routing-event parsed-path)))

(def history (atom nil))

(defn start! []
  (when (nil? @history)
    (reset! history (pushy/pushy dispatch-route parse-path)))
  (pushy/start! @history))

(def url-for (partial silk/depart routes))

(defn redirect-to [& args]
  (when @history
    (let [path (apply url-for args)
          self-redirect (= path (pushy/get-token @history))]
      (pushy/set-token! @history path)
      (when self-redirect                                   ; If we are re-directing to itself, we need to re-trigger routing manually.
        (when-let [parsed-path (parse-path path)]
          (dispatch-route parsed-path))))))

(re-frame/register-handler
  :redirect-to
  (fn [db [& args]]
    (apply redirect-to (drop 1 args))
    db))

(defmulti display-page :name)
(defmethod display-page :default [_current-route db]
  db)

(re-frame/register-handler
  :set-current-route
  (fn [db [_name current-route]]
    (display-page current-route (assoc db :current-route current-route))))

(re-frame/register-sub
  :current-route
  (fn [db _]
    (ratom/reaction (:current-route @db))))
