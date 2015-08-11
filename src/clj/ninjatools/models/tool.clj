;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.models.tool
  (:require [ninjatools.db.core :as db]
            [camel-snake-kebab.core :as csk]))

(defn- fill-in-fields [tool]
  (merge {:slug        (csk/->snake_case (.toLowerCase (:name tool)))
          :description ""} tool))

(defn create [tool]
  (let [tool (fill-in-fields tool)]
    (db/create-tool<! tool)))

(defn get-integrations-for [tool_id]
  (as-> (db/get-integrations-for {:tool_id tool_id}) integrations
        (mapcat (fn [i] [(:tool_a_id i) (:tool_b_id i)]) integrations)
        (set integrations)
        (disj integrations tool_id)))