;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.handlers
  (:require [re-frame.core :as re-frame]
            [ninjatools.db :as db]
            [ajax.core :as ajax]
            [ninjatools.util :refer [log dissoc-in]]
            [clojure.walk]
            [validateur.validation :as validateur]
            [ninjatools.models.user-schema :as user-schema]))

(defn report-unexpected-error [{:keys [status status-text]}]
  (js/alert "We are sorry, there was an unexpected error.")
  (log "Error: " status status-text))

(re-frame/register-handler
  :initialize-db
  (fn [_ _]
    (re-frame/dispatch [:get-current-user])
    {:current-route     nil
     :alerts            (sorted-map)
     :current-user      nil
     :log-in-form       {}
     :registration-form {}
     :tools             {:by-id   {}
                         :by-slug {}}
     :tools-in-use      #{}}))

(defmulti display-page :name)

(defmethod display-page :default [_current-route db]
  db)

(defmethod display-page :home [_current-route db]
  (when (empty? (get-in db [:tools :by-id]))
    (re-frame/dispatch [:get-tools]))
  db)

(defmethod display-page :tools [_current-route db]
  (when (empty? (get-in db [:tools :by-id]))
    (re-frame/dispatch [:get-tools]))
  db)

(defmethod display-page :tool [current-route db]
  (re-frame/dispatch [:get-tool-with-integrations (:slug current-route)])
  (assoc db :current-tool-slug (:slug current-route)))

(re-frame/register-handler
  :set-current-route
  (fn [db [_name current-route]]
    (display-page current-route (assoc db :current-route current-route))))

(re-frame/register-handler
  :remove-alert
  (fn [db [_ id]]
    (dissoc-in db [:alerts id])))

(re-frame/register-handler
  :get-current-user
  (fn [db [_]]
    (ajax/GET "/api/v1/current-user"
              {:handler       #(re-frame/dispatch [:got-current-user (clojure.walk/keywordize-keys %1)])
               :error-handler report-unexpected-error})
    db))

(re-frame/register-handler
  :got-current-user
  (fn [db [_ user]]
    (assoc db :current-user (if (empty? user) nil user))))

(re-frame/register-handler
  :log-out
  (fn [db [_]]
    (ajax/PUT "/api/v1/log-out" {:handler       (fn [_] (re-frame/dispatch [:logged-out]))
                                 :error-handler report-unexpected-error})
    db))

(re-frame/register-handler
  :logged-out
  (fn [db [_]]
    (-> db
        (assoc :current-user nil)
        (db/add-alert :success "You are now logged out."))))

(re-frame/register-handler
  :update-log-in-form
  (fn [db [_ ks value]]
    (let [db (assoc-in db (cons :log-in-form ks) value)]
      (if (nil? (get-in db [:log-in-form :errors]))
        db
        (assoc-in db [:log-in-form :errors] (user-schema/log-in-validation (:log-in-form db)))))))

(re-frame/register-handler
  :log-in
  (fn [db [_]]
    (let [log-in-form (:log-in-form db)]
      (if (validateur/valid? user-schema/log-in-validation log-in-form)
        (do (ajax/PUT "/api/v1/log-in"
                      {:params        (dissoc log-in-form :errors)
                       :handler       #(re-frame/dispatch [:got-logged-in (clojure.walk/keywordize-keys %1)])
                       :error-handler report-unexpected-error})
            db)
        (assoc-in db [:log-in-form :errors] (user-schema/log-in-validation log-in-form))))))

(re-frame/register-handler
  :got-logged-in
  (fn [db [_ {status :status log-in-form :log-in-form user :user}]]
    (if (= status "success")
      (-> db
          (assoc :log-in-form {}
                 :current-user user)
          (db/add-alert :success "You are now logged in."))
      (assoc db :log-in-form log-in-form))))

(re-frame/register-handler
  :update-registration-form
  (fn [db [_ ks value]]
    (let [db (assoc-in db (cons :registration-form ks) value)]
      (if (nil? (get-in db [:registration-form :errors]))
        db
        (assoc-in db [:registration-form :errors] (user-schema/registration-validation (:registration-form db)))))))

(re-frame/register-handler
  :register
  (fn [db [_]]
    (let [registration-form (:registration-form db)]
      (if (validateur/valid? user-schema/registration-validation registration-form)
        (do (ajax/POST "/api/v1/register"
                       {:params        (dissoc registration-form :errors)
                        :handler       #(re-frame/dispatch [:got-registered (clojure.walk/keywordize-keys %1)])
                        :error-handler report-unexpected-error})
            db)
        (assoc-in db [:registration-form :errors] (user-schema/registration-validation registration-form))))))

(re-frame/register-handler
  :got-registered
  (fn [db [_ {status :status registration-form :registration-form user :user}]]
    (if (= status "success")
      (-> db
          (assoc :registration-form {}
                 :current-user user)
          (db/add-alert :success "Thank you for registering, you are now also logged in with your new account."))
      (assoc db :registration-form registration-form))))

(re-frame/register-handler
  :get-tools
  (fn [db [_]]
    (ajax/GET "/api/v1/tools"
              {:handler       #(re-frame/dispatch [:got-tools %1])
               :error-handler report-unexpected-error})
    db))

(re-frame/register-handler
  :got-tools
  (fn [db [_ tools]]
    (let [tools (map clojure.walk/keywordize-keys tools)]
      (assoc db :tools {:by-id   (reduce #(assoc %1 (:id %2) %2) {} tools)
                        :by-slug (reduce #(assoc %1 (:slug %2) %2) {} tools)}))))

(re-frame/register-handler
  :get-tool-with-integrations
  (fn [db [_ tool-slug tool-requested]]
    (if-let [tool (get-in db [:tools :by-slug tool-slug])]
      (when (empty? (:integration-ids tool))
        (ajax/GET (str "/api/v1/tools/" (:id tool) "/integrations")
                  {:handler       #(re-frame/dispatch [:got-integrations (:id tool) %1])
                   :error-handler report-unexpected-error}))
      (do (when (not tool-requested)
            (re-frame/dispatch [:get-tools]))               ; TODO: only get the tool we want, by slug.
          (re-frame/dispatch [:get-tool-with-integrations tool-slug true])))
    db))

(re-frame/register-handler
  :got-integrations
  (fn [db [_ tool-id integration-ids]]
    (let [tool (assoc (get-in db [:tools :by-id tool-id]) :integration-ids integration-ids)]
      (assoc-in db [:tools :by-id tool-id] tool))))         ; TODO: get the tools that we have integration ids for when we stop getting all the tools all the time.

(re-frame/register-handler
  :mark-tool-as-used
  (fn [db [_ tool-id]]
    (update-in db [:tools-in-use] conj tool-id)))
