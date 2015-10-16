;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.db
  (:require [cljs-uuid-utils.core :as uuid]
            [schema.core :as s :include-macros true]
            [ninjatools.util :refer [dissoc-in]]))

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
