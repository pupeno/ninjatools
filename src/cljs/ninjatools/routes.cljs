;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.routes
  (:require [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [re-frame.core :as re-frame]))

(def routes ["/" {""              :tools
                  ["tool/" :slug] :tool
                  "about"         :about}])

(defn parse-path [path]
  (bidi/match-route routes path))

(defn start! []
  (pushy/start!
    (pushy/pushy (fn [matched-route]
                   (.log js/console (pr-str matched-route))
                   (let [event-name (keyword (str "display-page-" (name (:handler matched-route))))]
                     (re-frame/dispatch [event-name (:route-params matched-route)])))
                 parse-path)))

(def url-for (partial bidi/path-for routes))
