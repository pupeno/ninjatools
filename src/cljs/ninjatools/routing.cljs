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

(def history (atom nil))

(defn start! []
  (when (nil? @history)
    (reset! history (pushy/pushy (fn [matched-route]
                                   (re-frame/dispatch [:ui-interaction])
                                   (re-frame/dispatch (routing-event matched-route)))
                                 parse-path)))
  (pushy/start! @history))

(def url-for (partial silk/depart routes))

(defn redirect-to [& args]
  (when @history
    (pushy/set-token! @history (apply url-for args))))

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
