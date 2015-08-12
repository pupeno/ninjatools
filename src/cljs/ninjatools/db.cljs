;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.db)

(defn get-tool-by-slug [db slug]
  (when-let [tool-id (get-in db [:tools :by-slug slug])]
    (when-let [tool (get-in db [:tools :data tool-id])]
      tool)))
