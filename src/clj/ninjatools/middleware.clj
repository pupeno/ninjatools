;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.middleware
  (:require [ninjatools.layout :refer [*app-context* error-page]]
            [jdbc-ring-session.core :as jdbc-session-store]
            [to-jdbc-uri.core :refer [to-jdbc-uri]]
            [taoensso.timbre :as timbre]
            [environ.core :refer [env]]
            [selmer.middleware :as selmer]
            [prone.middleware :as prone]
            [ring.util.response :refer [redirect]]
            [ring.middleware.reload :as reload]
            [ring.middleware.webjars :refer [wrap-webjars]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [yeller.clojure.ring :as yeller]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth.accessrules :refer [restrict]]
            [buddy.auth :refer [authenticated?]]
            [ninjatools.layout :refer [*identity*]]
            [ring.middleware.ssl :refer [wrap-forwarded-scheme wrap-hsts wrap-ssl-redirect]]
            [ninjatools.db.core :as db]
            [ninjatools.util :refer [dissoc-in]]
            [ninjatools.util :as util])
  (:import [javax.servlet ServletContext]))

(defn wrap-context [handler]
  (fn [request]
    (binding [*app-context*
              (if-let [context (:servlet-context request)]
                ;; If we're not inside a servlet environment
                ;; (for example when using mock requests), then
                ;; .getContextPath might not exist
                (try (.getContextPath ^ServletContext context)
                     (catch IllegalArgumentException _ context))
                ;; if the context is not specified in the request
                ;; we check if one has been specified in the environment
                ;; instead
                (:app-context env))]
      (handler request))))

(defn wrap-delay [handler]
  (fn [request]
    (Thread/sleep 5000)
    (handler request)))

(defn wrap-reload [handler]
  (if (env :dev)
    (reload/wrap-reload handler)
    handler))

(defn wrap-csrf [handler]
  (wrap-anti-forgery
    handler
    {:error-response
     (error-page
       {:status 403
        :title  "Invalid anti-forgery token"})}))

(defn wrap-formats [handler]
  (wrap-restful-format handler {:formats [:json-kw :transit-json :transit-msgpack]}))

(defn on-error [request response]
  (error-page
    {:status 403
     :title  (str "Access to " (:uri request) " is not authorized")}))

(defn wrap-restricted [handler]
  (restrict handler {:handler  authenticated?
                     :on-error on-error}))

(defn wrap-identity [handler]
  (fn [request]
    (if-let [current-user-id (get-in request [:session :identity])]
      (if-let [current-user (when current-user-id (db/get-user-by-id current-user-id))]
        (binding [*identity* (get-in request [:session :identity])] ; This original line came from Luminus, when it was expected that views might want to access *identity*, which is not exected in a SPA, so, we may want to remove it. Not sure yet.
          (handler (assoc request :current-user current-user)))
        (do (util/report-error "There's an identity in the session but the user with that id cannot be found."
                               (util/format-request-for-error request))
            (handler (-> request
                         (dissoc :identity)
                         (dissoc-in [:session :identity])))))
      (handler request))))

(defn wrap-auth [handler]
  (-> handler
      wrap-identity
      (wrap-authentication (session-backend))))

(defn wrap-ssl [handler]
  (if (or (env :dev) (env :test))
    handler
    (-> handler
        wrap-hsts
        wrap-ssl-redirect
        wrap-forwarded-scheme)))

(defn wrap-error [handler]
  (if (env :dev)
    (-> handler
        reload/wrap-reload
        selmer/wrap-error-page
        prone/wrap-exceptions)
    (fn [request]
      (try
        (if (env :test)
          (handler request)
          ((yeller/wrap-ring handler {:token (:yeller-token env) :environment (:environment env)}) request))
        (catch Throwable t
          (timbre/error t)
          (error-page {:status  500
                       :title   "Something very bad has happened!"
                       :message "We've dispatched a team of highly trained gnomes to take care of the problem."}))))))

(defn wrap-base [handler]
  (-> handler
      wrap-auth
      wrap-formats
      wrap-webjars
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)      ; Anti-forgery is not applied to the API, only to the served HTML (defined in handler.clj) and even then its need is doubious in an SPA.
            (assoc-in [:session :store] (jdbc-session-store/jdbc-store (to-jdbc-uri (env :database-url)) #_{:table :sessions})) ; TODO: switch to table sessions once this has been fixed: https://github.com/yogthos/jdbc-ring-session/issues/4
            (assoc-in [:session :secure] (not (or (env :dev) (env :test))))))
      wrap-context
      wrap-ssl
      wrap-error
      wrap-reload))
