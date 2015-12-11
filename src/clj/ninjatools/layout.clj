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
            ring.util.request
            [prerenderer.core :as prerenderer]))

(declare ^:dynamic *identity*)
(declare ^:dynamic *app-context*)
(parser/set-resource-path! (clojure.java.io/resource "templates"))
(parser/add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))
(filters/add-filter! :markdown (fn [content] [:safe (md-to-html-string content)]))

(def js-engine (atom nil))                                  ; Set by ninjatools.core

(defn render [request]
  (-> "app.html"
      (parser/render-file
        (assoc {}
          :dev (env :dev)
          :csrf-token *anti-forgery-token*
          :app-context *app-context*
          :identity *identity*
          :yeller-token (:yeller-token env)
          :environment (:environment env)
          :prerendered-content [:safe (prerenderer/render @js-engine (ring.util.request/request-url request) (:headers request))]))
      response
      (content-type "text/html; charset=utf-8")))

(defn error-page
  "error-details should be a map containing the following keys:
   :status - error status
   :title - error title (optional)
   :message - detailed error message (optional)

   returns a response map with the error page as the body and the status specified by the status key"
  [error-details]
  {:status  (:status error-details)
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    (parser/render-file "error.html" error-details)})
