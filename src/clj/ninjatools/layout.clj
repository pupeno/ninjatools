;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.layout
  (:require [selmer.parser :as parser]
            [selmer.filters :as filters]
            [markdown.core :refer [md-to-html-string]]
            [ring.util.response :refer [content-type response]]
            [compojure.response :refer [Renderable]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [environ.core :refer [env]]
            [prerenderer.core :as prerenderer]))

(declare ^:dynamic *identity*)
(declare ^:dynamic *servlet-context*)
(parser/set-resource-path! (clojure.java.io/resource "templates"))
(parser/add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))
(filters/add-filter! :markdown (fn [content] [:safe (md-to-html-string content)]))

(def engine (prerenderer/create {:resource          "public/js/server-side.js"
                                 :wait-for-resource (env :dev)}))

(defn render [request]
  (-> "app.html"
      (parser/render-file
        (assoc {}
          :dev (env :dev)
          :csrf-token *anti-forgery-token*
          :servlet-context *servlet-context*
          :identity *identity*
          :prerendered-content [:safe (prerenderer/render engine request)]))
      response
      (content-type "text/html; charset=utf-8")))
