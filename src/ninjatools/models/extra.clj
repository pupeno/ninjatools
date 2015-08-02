;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.models.extra
  (:require [ninjatools.db.core :as db]
            [ninjatools.models.tool :as tool]))

(def tools
  [{:name "GitHub" :url "https://github.com"}
   {:name "BitBucket" :url "https://bitbucket.org"}
   {:name "Slack" :url "https://slack.com"}
   {:name "Flowdock" :url "https://flowdock.com"}])

(defn repopulate-db! []
  (db/delete-all!)
  (for [tool tools]
    (println (tool/create tool))))
