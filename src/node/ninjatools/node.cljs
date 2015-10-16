;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.node
  (:require [cljs.nodejs :as nodejs]
            [prerenderer.core :as prerenderer]
            [prerenderer.re-frame :as re-frame-prerenderer]
            ninjatools.core                                 ; To include all of ninja namespaces.
            [ninjatools.routing :as routes]
            [ninjatools.views :as views]
            [reagent.core :as reagent]))

(def url (nodejs/require "url"))

(defn render [req res]
  (let [page-path (.-path (.parse url (.-url (.-query req))))
        matched-route (routes/parse-path page-path)
        event (routes/routing-event matched-route)]
    (if (nil? matched-route)
      (throw (js/Error. (str "Unrecognized path: " page-path)))
      (re-frame-prerenderer/dispatch-super-sync
        event
        (fn [] (.send res (reagent/render-to-string [views/main-panel])))))))

(set! *main-cli-fn* (prerenderer/create render "Ninja Tools"))
