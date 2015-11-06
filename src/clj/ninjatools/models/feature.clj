;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.models.feature
  (:require [ninjatools.db.core :as db]
            [camel-snake-kebab.core :as csk]
            [conman.core :as conman]))

(defn add-wanted-features [user-id feature-ids]
  (conman/with-transaction [t-conn db/conn]
    (let [wanted-features (db/get-wanted-features {:user-id user-id})
          wanted-feature-ids (set (map :id wanted-features))
          feature-ids (set feature-ids)]
      (doseq [feature-id feature-ids]
        (when (not (some #{feature-id} wanted-feature-ids))
          (db/add-wanted-feature<! {:user-id user-id
                               :feature-id feature-id})))
      (clojure.set/union wanted-feature-ids feature-ids))))

(defn add-wanted-feature [user-id feature-id]
  (add-wanted-features user-id (set [feature-id])))
