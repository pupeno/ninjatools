;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.db
  (:require [cljs-uuid-utils.core :as uuid]
            [schema.core :as s :include-macros true]))

(s/defn ^:always-validate add-alert [db
                                     type :- (s/enum :success :info :warning :danger)
                                     message]
  (update db :alerts (fn [alerts]
                       (let [id (uuid/make-random-squuid)]
                         (assoc alerts id
                                       {:type    type
                                        :message message})))))
