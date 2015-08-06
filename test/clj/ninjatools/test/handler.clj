;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.test.handler
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [ninjatools.handler :refer :all]))

(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= 404 (:status response))))))
