;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.alerts
  (:require [schema.core :as s :include-macros true]
            [reagent.ratom :as ratom :include-macros true]
            [re-frame.core :as re-frame]
            [cljs-uuid-utils.core :as uuid]
            [ninjatools.util :refer [dissoc-in]]
            [ninjatools.human :as human]))

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
   (remove-old-alerts db 60))
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

(re-frame/register-handler ; See to-do in human.cljs.
  :human-interaction
  (fn [db & _]
    (remove-old-alerts db)))

(re-frame/register-sub
  :alerts
  (fn [db _]
    (ratom/reaction (:alerts @db))))

(defn view []
  (let [alerts (re-frame/subscribe [:alerts])]
    (fn []
      (if (not (empty? @alerts))
        [:div.alerts
         (map (fn [[id alert]]
                [:div {:key   id
                       :class (str "alert alert-" (name (:type alert)))
                       :role  "alert"}
                 (:message alert)
                 [:button {:type       "button"
                           :class      "close"
                           :aria-label "Close"
                           :on-click   #(human/dispatch [:remove-alert id])}
                  [:span {:aria-hidden true} [:i.fa.fa-times]]]])
              @alerts)]))))
