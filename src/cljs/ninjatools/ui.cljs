;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.ui
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

(defn dispatch
  "Re-frame dispatch enhanced for UI. On top of re-frame-dispatching args, it:
  * stops the default handling from happening, so links and forms don't get submitted.
  * it dispatches ui-interaction, currently used to hide old alerts
  * passes the target of the event (the form or link) as the last argument to the re-frame-dispatch."
  [js-event event]
  (.preventDefault js-event)
  (re-frame/dispatch [:ui-interaction])
  (re-frame/dispatch (conj event (.-target js-event))))

; TODO: implement a way for many functions to be called when ui-interaction is dispatched. Right now we don't need  this as we have only one event handler in alerts.cljs.

(defn loading
  "Display a loading panel"
  []
  [:div "Loading..."])

(def css-transition-group
  (reagent/adapt-react-class js/React.addons.CSSTransitionGroup))
