;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.node
  (:require [prerenderer.core :as prerenderer]
            [prerenderer.re-frame :as re-frame-prerenderer]
            [re-frame.core :as re-frame]
            ninjatools.core                                 ; To include all of ninja namespaces.
            [ninjatools.routing :as routes]
            [ninjatools.layout :as views])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))


(defn render-and-send [page-path send-to-browser]
  (let [matched-route (routes/parse-path page-path)
        event (routes/routing-event matched-route)]
    (if (nil? matched-route)
      (throw (js/Error. (str "Unrecognized path: " page-path)))
      (do (re-frame/dispatch-sync event)
          (re-frame-prerenderer/render-by-timeout [views/main-panel] send-to-browser)))))

(set! *main-cli-fn* (prerenderer/create render-and-send "Ninja Tools"))
