;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.models.tool
  (:require [ninjatools.db.core :as db]
            [camel-snake-kebab.core :as csk]
            [conman.core :as conman]))

(defn- fill-in-fields [tool]
  (merge {:slug        (csk/->snake_case (.toLowerCase (:name tool)))
          :description ""} tool))

(defn create [tool]
  (let [tool (fill-in-fields tool)]
    (db/create-tool<! tool)))

(defn get-integrations-for [tool-id]
  (as-> (db/get-integrations-for {:tool-id tool-id}) integrations
        (mapcat (fn [i] [(:tool-a-id i) (:tool-b-id i)]) integrations)
        (set integrations)
        (disj integrations tool-id)))

(defn add-used-tools [user-id tool-ids]
  (conman/with-transaction [t-conn db/conn]
    (let [used-tools (db/get-used-tools {:user-id user-id})
          used-tool-ids (set (map :id used-tools))
          tool-ids (set tool-ids)]
      (doseq [tool-id tool-ids]
        (when (not (some #{tool-id} used-tool-ids))
          (db/add-used-tool<! {:user-id user-id
                               :tool-id tool-id})))
      (clojure.set/union used-tool-ids tool-ids))))

(defn add-used-tool [user-id tool-id]
  (add-used-tools user-id (set [tool-id])))

(defn get-suggested-tools-for-feature [tool-ids]
  (fn [feature-id]
    (map
      #(assoc % :integration-ids (clojure.set/intersection tool-ids (get-integrations-for (:id %))))
      (db/get-tools-for-wanted-feature feature-id (vec tool-ids)))))

(defn get-suggested-tools
  ([user-id]
   (let [feature-ids (map :feature-id (db/get-wanted-features {:user-id user-id}))
         tool-ids (set (map :tool-id (db/get-used-tools {:user-id user-id})))]
     (get-suggested-tools feature-ids tool-ids)))
  ([feature-ids tool-ids] (mapcat (get-suggested-tools-for-feature tool-ids) feature-ids)))
