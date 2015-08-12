;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.routes
  (:require [clojure.set :refer [rename-keys]]
            [domkm.silk :as silk]
            [pushy.core :as pushy]
            [re-frame.core :as re-frame]))

(def routes (silk/routes [[:tools [[]]]
                          [:tool [["tool" :slug]]]
                          [:about [["about"]]]]))

(def history
  (pushy/pushy (fn [matched-route]
                 (let [matched-route (rename-keys matched-route {:domkm.silk/name    :name
                                                                 :domkm.silk/pattern :pattern
                                                                 :domkm.silk/routes  :routes
                                                                 :domkm.silk/url     :url})
                       event-name (keyword (str "display-page-" (name (:name matched-route))))]
                   (re-frame/dispatch [event-name matched-route])))
               (fn [url]
                 (silk/arrive routes url))))

(defn start! []
  (pushy/start! history))

(def url-for (partial silk/depart routes))