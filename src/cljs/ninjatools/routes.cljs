;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.routes
  (:require [clojure.set :refer [rename-keys]]
            [domkm.silk :as silk]
            [pushy.core :as pushy]
            [re-frame.core :as re-frame]))

(def routes (silk/routes [[:home [[]]]
                          [:tools [["tools"]]]
                          [:tool [["tool" :slug]]]
                          [:about [["about"]]]
                          [:login [["login"]]]]))

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
  (pushy/start! (pushy/pushy #(re-frame/dispatch (routing-event %))
                             parse-path)))

(def url-for (partial silk/depart routes))
