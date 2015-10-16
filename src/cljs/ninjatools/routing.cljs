;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.routing
  (:require [clojure.set :refer [rename-keys]]
            [domkm.silk :as silk]
            [pushy.core :as pushy]
            [re-frame.core :as re-frame]
            [reagent.ratom :as ratom :include-macros true]))

(def routes (silk/routes [[:home [[]]]
                          [:tools [["tools"]]]
                          [:tool [["tool" :slug]]]
                          [:about [["about"]]]
                          [:register [["register"]]]
                          [:log-in [["login"]]]]))

(defn sanitize-silk-keywords [matched-route]
  (rename-keys matched-route {:domkm.silk/name    :name
                              :domkm.silk/pattern :pattern
                              :domkm.silk/routes  :routes
                              :domkm.silk/url     :url}))

(defn parse-path [path]
  (sanitize-silk-keywords (silk/arrive routes path)))

(defn routing-event [matched-route]
  [:set-current-route matched-route])

(defn start! []
  (pushy/start! (pushy/pushy (fn [matched-route]
                               (re-frame/dispatch [:human-interaction])
                               (re-frame/dispatch (routing-event matched-route)))
                             parse-path)))

(def url-for (partial silk/depart routes))

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
