;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.models.extra
  (:require [ninjatools.db.core :as db]
            [ninjatools.models.tool :as tool]))

(def tools
  [{:name "GitHub" :url "https://github.com"}
   {:name "BitBucket" :url "https://bitbucket.org"}

   {:name "Atom" :url "https://atom.io/"}
   {:name "Visual Studio" :url "http://www.visualstudio.com/"}
   {:name "eclipse" :url "http://eclipse.github.io/"}
   {:name "CloudBees" :url "http://www.cloudbees.com/"}
   {:name "Travis CI" :url "https://travis-ci.com/"}
   {:name "circleci" :url "https://circleci.com/"}

   {:name "Slack" :url "https://slack.com"}
   {:name "asana" :url "https://asana.com/"}
   {:name "flowdock" :url "https://flowdock.com"}
   {:name "zendesk" :url "https://www.zendesk.com/"}
   {:name "PivotalTracker" :url "http://www.pivotaltracker.com/"}
   {:name "ZenHub.io" :url "https://www.zenhub.io/"}

   {:name "amazon web services" :url "http://aws.amazon.com/"}
   {:name "Windows Azure" :url "http://www.windowsazure.com/"}
   {:name "Google Cloud Platform" :url "https://cloud.google.com/"}
   {:name "heroku" :url "https://www.heroku.com/"}])

(def integrations
  [["GitHub" "Atom"]
   ["GitHub" "Visual Studio"]
   ["GitHub" "eclipse"]
   ["GitHub" "CloudBees"]
   ["GitHub" "Travis CI"]
   ["GitHub" "circleci"]
   ["GitHub" "Slack"]
   ["GitHub" "asana"]
   ["GitHub" "flowdock"]
   ["GitHub" "zendesk"]
   ["GitHub" "PivotalTracker"]
   ["GitHub" "ZenHub.io"]
   ["GitHub" "amazon web services"]
   ["GitHub" "Windows Azure"]
   ["GitHub" "Google Cloud Platform"]
   ["GitHub" "heroku"]
   ["BitBucket" "Slack"]
   ["BitBucket" "flowdock"]])

(defn repopulate-db! []
  (db/delete-all!)
  (doall (for [tool tools]
           (println "Created tool: " (tool/create tool))))
  (doall (for [integration integrations
               :let [[tool_a tool_b] (map db/get-tool-by-name (sort integration))]]
           (do
             (println "Creating integration between " tool_a " and " tool_b)
             (db/create-integration<! {:tool_a_id (:id tool_a)
                                       :tool_b_id (:id tool_b)
                                       :comment   ""})))))
