;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.routes
  (:require [clojure.set :refer [rename-keys]]
            [domkm.silk :as silk]
            [pushy.core :as pushy]
            [re-frame.core :as re-frame]))

(def routes (silk/routes [[:home [[]]]
                          [:tools [["tools"]]]
                          [:tool [["tool" :slug]]]
                          [:about [["about"]]]]))

(defn sanitize-silk-keywords [matched-route]
  (rename-keys matched-route {:domkm.silk/name    :name
                              :domkm.silk/pattern :pattern
                              :domkm.silk/routes  :routes
                              :domkm.silk/url     :url}))

(defn parse-path [path]
  (sanitize-silk-keywords (silk/arrive routes path)))

(defn routing-event [matched-route]
  [:set-current-route matched-route])

;; Similar to Pushy's, but it considers entirely blank URIs not processable. Related to this: https://github.com/kibu-australia/pushy/issues/10
(defn processable-url? [uri]
  (and (not (clojure.string/blank? uri))
       (or (not (.hasDomain uri))
           (some? (re-matches (re-pattern (str "^" (.-origin js/location) ".*$"))
                              (str uri))))))

(defn start! []
  (pushy/start! (pushy/pushy #(re-frame/dispatch (routing-event %))
                             parse-path
                             :processable-url? processable-url?)))

(def url-for (partial silk/depart routes))
