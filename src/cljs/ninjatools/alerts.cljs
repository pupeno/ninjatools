;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.alerts
  (:require [schema.core :as s :include-macros true]
            [reagent.ratom :as ratom :include-macros true]
            [re-frame.core :as re-frame]
            [cljs-uuid-utils.core :as uuid]
            [ninjatools.util :refer [dissoc-in]]
            [ninjatools.ui :as ui]))

(s/defn ^:always-validate add-alert [db
                                     type :- (s/enum :success :info :warning :danger)
                                     message]
  (update db :alerts (fn [alerts]
                       (let [id (uuid/make-random-squuid)]
                         (assoc alerts id
                                       {:type     type
                                        :message  message
                                        :shown-at (.getTime (js/Date.))})))))

(defn remove-alert [db id]
  (dissoc-in db [:alerts id]))

(defn remove-old-alerts
  ([db]
   (remove-old-alerts db 20))
  ([db timeout]
   (update db :alerts
           (fn [alerts]
             (into {} (filter (fn [[_ alert]]
                                (> (* timeout 1000)
                                   (- (.getTime (js/Date.))
                                      (:shown-at alert))))
                              alerts))))))

(re-frame/register-handler
  :remove-alert
  (fn [db [_ id]]
    (remove-alert db id)))

(re-frame/register-handler                                  ; See to-do in ui.cljs.
  :ui-interaction
  (fn [db & _]
    (remove-old-alerts db)))

(re-frame/register-sub
  :alerts
  (fn [db _]
    (ratom/reaction (:alerts @db))))

(defn view []
  (let [alerts (re-frame/subscribe [:alerts])]
    (fn []
      [:div.alerts
       [ui/css-transition-group {:transition-name "alert" :transition-enter-timeout 300 :transition-leave-timeout 300}
        (if (not (empty? @alerts))
          (map (fn [[id alert]]
                 [:div.alert {:key   id
                              :class (str "alert-" (name (:type alert)))
                              :role  "alert"}
                  (:message alert)
                  [:button {:type       "button"
                            :class      "close"
                            :aria-label "Close"
                            :on-click   #(re-frame/dispatch [:remove-alert id])} ; We don't use ui/dispatch because ui/dispatch, as a side effect, can remove alerts, and when you are removing alerts, it's a bad user experience for other alerts to disapear too.
                   [:span {:aria-hidden true} [:i.fa.fa-times]]]])
               @alerts))]])))
