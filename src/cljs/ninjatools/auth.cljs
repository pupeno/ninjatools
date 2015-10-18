;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.


(ns ninjatools.auth
  (:require [reagent.ratom :as ratom :include-macros true]
            [re-frame.core :as re-frame]
            [ajax.core :as ajax]
            [free-form.re-frame :as forms]
            [validateur.validation :as validateur]
            [ninjatools.models.user-schema :as user-schema]
            [ninjatools.layout :as layout]
            [ninjatools.alerts :as alerts]
            [ninjatools.ui :as ui]
            [ninjatools.util :as util]))

(re-frame/register-handler
  :get-current-user
  (fn [db [_]]
    (ajax/GET "/api/v1/current-user"
              {:handler       #(re-frame/dispatch [:got-current-user (clojure.walk/keywordize-keys %1)])
               :error-handler util/report-unexpected-error})
    db))

(re-frame/register-handler
  :got-current-user
  (fn [db [_ user]]
    (assoc db :current-user (if (empty? user) nil user))))

(re-frame/register-sub
  :current-user
  (fn [db _]
    (ratom/reaction (:current-user @db))))

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
                        :error-handler util/report-unexpected-error})
            db)
        (assoc-in db [:registration-form :errors] (user-schema/registration-validation registration-form))))))

(re-frame/register-handler
  :got-registered
  (fn [db [_ {status :status registration-form :registration-form user :user}]]
    (if (= status "success")
      (-> db
          (assoc :registration-form {}
                 :current-user user)
          (alerts/add-alert :success "Thank you for registering, you are now also logged in with your new account."))
      (assoc db :registration-form registration-form))))


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
                       :error-handler util/report-unexpected-error})
            db)
        (assoc-in db [:log-in-form :errors] (user-schema/log-in-validation log-in-form))))))

(re-frame/register-handler
  :got-logged-in
  (fn [db [_ {status :status log-in-form :log-in-form user :user}]]
    (if (= status "success")
      (-> db
          (assoc :log-in-form {}
                 :current-user user)
          (alerts/add-alert :success "You are now logged in."))
      (assoc db :log-in-form log-in-form))))

(re-frame/register-handler
  :log-out
  (fn [db [_]]
    (ajax/PUT "/api/v1/log-out" {:handler       (fn [_] (re-frame/dispatch [:logged-out]))
                                 :error-handler util/report-unexpected-error})
    db))

(re-frame/register-handler
  :logged-out
  (fn [db [_]]
    (-> db
        (assoc :current-user nil)
        (alerts/add-alert :success "You are now logged out."))))

(re-frame/register-sub
  :registration-form
  (fn [db _]
    (ratom/reaction (:registration-form @db))))

(re-frame/register-sub
  :log-in-form
  (fn [db _]
    (ratom/reaction (:log-in-form @db))))

(defn log-in-page []
  (let [log-in-form (re-frame/subscribe [:log-in-form])]
    (fn []
      [:div
       [:h1 "Log in"]
       [forms/form @log-in-form (:errors @log-in-form) :update-log-in-form
        [:form.form-horizontal {:on-submit #(ui/dispatch % [:log-in])}
         [:div.col-sm-offset-2.col-sm-10 {:free-form/error-message {:key :-general}} [:p.text-danger]]
         [:div.form-group {:free-form/error-class {:key :email :error "has-error"}}
          [:label.col-sm-2.control-label {:for :email} "Email"]
          [:div.col-sm-10 [:input.form-control {:free-form/field {:key :email}
                                                :type            :email
                                                :id              :email
                                                :placeholder     "sam@example.com"}]
           [:div.text-danger {:free-form/error-message {:key :email}} [:p]]]]
         [:div.form-group {:free-form/error-class {:ks [:password] :error "has-error"}}
          [:label.col-sm-2.control-label {:for :password} "Password"]
          [:div.col-sm-10 [:input.form-control {:free-form/field {:ks [:password]}
                                                :type            :password
                                                :id              :password}]
           [:div.text-danger {:free-form/error-message {:ks [:password]}} [:p]]]]
         [:div.form-group
          [:div.col-sm-offset-2.col-sm-10
           [:button.btn.btn-primary {:type :submit} "Log in"]]]]]])))

(defmethod layout/pages :log-in [] [log-in-page])

(defn register-page []
  (let [registration-form (re-frame/subscribe [:registration-form])]
    (fn []
      [:div
       [:h1 "Register"]
       [forms/form @registration-form (:errors @registration-form) :update-registration-form
        [:form.form-horizontal {:on-submit #(ui/dispatch % [:register])}
         [:div.form-group {:free-form/error-class {:key :email :error "has-error"}}
          [:label.col-sm-2.control-label {:for :email} "Email"]
          [:div.col-sm-10 [:input.form-control {:free-form/field {:key :email}
                                                :type            :email
                                                :id              :email
                                                :placeholder     "sam@example.com"}]
           [:div.text-danger {:free-form/error-message {:key :email}} [:p]]]]
         [:div.form-group {:free-form/error-class {:ks [:password] :error "has-error"}}
          [:label.col-sm-2.control-label {:for :password} "Password"]
          [:div.col-sm-10 [:input.form-control {:free-form/field {:ks [:password]}
                                                :type            :password
                                                :id              :password}]
           [:div.text-danger {:free-form/error-message {:ks [:password]}} [:p]]]]
         [:div.form-group {:free-form/error-class {:key :password-confirmation :error "has-error"}}
          [:label.col-sm-2.control-label {:for :password-confirmation} "Password confirmation"]
          [:div.col-sm-10 [:input.form-control {:free-form/field {:key :password-confirmation}
                                                :type            :password
                                                :id              :password-confirmation}]
           [:div.text-danger {:free-form/error-message {:key :password-confirmation}} [:p]]]]
         [:div.form-group
          [:div.col-sm-offset-2.col-sm-10
           [:button.btn.btn-primary {:type :submit} "Register"]]]]]])))

(defmethod layout/pages :register [] [register-page])
