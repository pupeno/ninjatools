;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.auth
  (:require [reagent.ratom :as ratom :include-macros true]
            [re-frame.core :as re-frame]
            [ajax.core :as ajax]
            [free-form.re-frame :as forms]
            [validateur.validation :as validateur]
            [ninjatools.models.user-schema :as user-schema]
            [ninjatools.layout :as layout]
            [ninjatools.routing :as routing]
            [ninjatools.alerts :as alerts]
            [ninjatools.ui :as ui]
            [ninjatools.util :as util :refer [dissoc-in]]))

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
      (if (nil? (get-in db [:registration-form :-errors]))
        db
        (assoc-in db [:registration-form :-errors] (user-schema/registration-validation (:registration-form db)))))))

(re-frame/register-handler
  :register
  (fn [db [_]]
    (let [registration-form (:registration-form db)]
      (if (validateur/valid? user-schema/registration-validation registration-form)
        (do (ajax/POST "/api/v1/register"
                       {:params        (dissoc registration-form :-errors)
                        :handler       #(re-frame/dispatch [:got-registered (clojure.walk/keywordize-keys %1)])
                        :error-handler (fn [error]
                                         (re-frame/dispatch [:clean-up-processing [:registration-form]])
                                         (util/report-unexpected-error error))})
            (assoc-in db [:registration-form :-processing] true))
        (assoc-in db [:registration-form :-errors] (user-schema/registration-validation registration-form))))))

(re-frame/register-handler
  :got-registered
  (fn [db [_ {status :status registration-form :registration-form user :user}]]
    (if (= status "success")
      (do
        (routing/redirect-to :home)
        (-> db
            (assoc :registration-form {}
                   :current-user user)
            (alerts/add-alert :success "Thank you for registering, you are now also logged in with your new account.")))
      (assoc db :registration-form registration-form))))

(re-frame/register-sub
  :registration-form
  (fn [db _]
    (ratom/reaction (:registration-form @db))))

(defmethod layout/pages :register [_]
  (let [registration-form (re-frame/subscribe [:registration-form])]
    (fn []
      [:div
       [:h1 "Register"]
       [forms/form @registration-form (:-errors @registration-form) :update-registration-form
        [:form.form-horizontal {:on-submit #(ui/dispatch % [:register])}
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
         [:div.form-group {:free-form/error-class {:key :password-confirmation :error "has-error"}}
          [:label.col-sm-2.control-label {:for :password-confirmation} "Password confirmation"]
          [:div.col-sm-10 [:input.form-control {:free-form/field {:key :password-confirmation}
                                                :type            :password
                                                :id              :password-confirmation}]
           [:div.text-danger {:free-form/error-message {:key :password-confirmation}} [:p]]]]
         [:div.form-group
          [:div.col-sm-offset-2.col-sm-5
           [:button.btn.btn-primary {:type :submit :disabled (:-processing @registration-form)}
            (if (:-processing @registration-form)
              "Registering, please wait..."
              "Register")]]
          [:div.col-sm-5.text-right
           [:p "Already have an account? "
            [:a {:href (routing/url-for :log-in)} "Log in"]]]]]]])))

(re-frame/register-handler
  :update-log-in-form
  (fn [db [_ ks value]]
    (let [db (assoc-in db (cons :log-in-form ks) value)]
      (if (nil? (get-in db [:log-in-form :-errors]))
        db
        (assoc-in db [:log-in-form :-errors] (user-schema/log-in-validation (:log-in-form db)))))))

(re-frame/register-handler
  :log-in
  (fn [db [_]]
    (let [log-in-form (:log-in-form db)]
      (if (validateur/valid? user-schema/log-in-validation log-in-form)
        (do (ajax/PUT "/api/v1/log-in"
                      {:params        (dissoc log-in-form :-errors)
                       :handler       #(re-frame/dispatch [:got-logged-in (clojure.walk/keywordize-keys %1)])
                       :error-handler (fn [error]
                                        (re-frame/dispatch [:clean-up-processing [:log-in-form]])
                                        (util/report-unexpected-error error))})
            (assoc-in db [:log-in-form :-processing] true))
        (assoc-in db [:log-in-form :-errors] (user-schema/log-in-validation log-in-form))))))

(re-frame/register-handler
  :got-logged-in
  (fn [db [_ {status :status log-in-form :log-in-form user :user}]]
    (if (= status "success")
      (do
        (routing/redirect-to :home)
        (-> db
            (assoc :log-in-form {}
                   :current-user user)
            (alerts/add-alert :success "You are now logged in.")))
      (assoc db :log-in-form log-in-form))))

(re-frame/register-sub
  :log-in-form
  (fn [db _]
    (ratom/reaction (:log-in-form @db))))

(re-frame/register-handler
  :log-out
  (fn [db [_]]
    (ajax/PUT "/api/v1/log-out" {:handler       (fn [_] (re-frame/dispatch [:logged-out]))
                                 :error-handler util/report-unexpected-error})
    db))

(re-frame/register-handler
  :logged-out
  (fn [db [_]]
    (routing/redirect-to :home)
    (-> db
        (assoc :current-user nil)
        (alerts/add-alert :success "You are now logged out."))))

(defmethod layout/pages :log-in [_]
  (let [log-in-form (re-frame/subscribe [:log-in-form])]
    (fn []
      [:div
       [:h1 "Log in"]
       [forms/form @log-in-form (:-errors @log-in-form) :update-log-in-form
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
          [:div.col-sm-offset-2.col-sm-5
           [:button.btn.btn-primary {:type :submit :disabled (:-processing @log-in-form)}
            (if (:-processing @log-in-form)
              "Logging in, please wait..."
              "Log in")]]
          [:div.col-sm-5.text-right
           [:p "Don't know your password? "
            [:a {:href (routing/url-for :reset-password)} "Reset Password"]]]]]]])))

(re-frame/register-handler
  :update-reset-password-form
  (fn [db [_ ks value]]
    (let [db (assoc-in db (cons :reset-password-form ks) value)]
      (if (nil? (get-in db [:reset-password-form :-errors]))
        db
        (assoc-in db [:reset-password-form :-errors] (user-schema/reset-password-validation (:reset-password-form db)))))))

(re-frame/register-handler
  :reset-password
  (fn [db [_ html-form]]
    (let [reset-password-form (:reset-password-form db)]
      (if (validateur/valid? user-schema/reset-password-validation reset-password-form)
        (do (ajax/POST "/api/v1/reset-password"
                       {:params        (dissoc reset-password-form :-errors)
                        :handler       #(re-frame/dispatch [:got-reset-password html-form (clojure.walk/keywordize-keys %1)])
                        :error-handler (fn [error]
                                         (re-frame/dispatch [:clean-up-processing [:reset-password-form]])
                                         (util/report-unexpected-error error))})
            (assoc-in db [:reset-password-form :-processing] true))
        (assoc-in db [:reset-password-form :-errors] (user-schema/reset-password-validation reset-password-form))))))

(re-frame/register-handler
  :got-reset-password
  (fn [db [_ html-form {status :status reset-password-form :reset-password-form}]]
    (if (= status "success")
      (do
        (.reset html-form)
        (-> db
            (assoc :reset-password-form {})
            (alerts/add-alert :success "If there was an account with that email address, an email was sent to reset the password.")))
      (assoc db :reset-password-form reset-password-form))))

(re-frame/register-sub
  :reset-password-form
  (fn [db _]
    (ratom/reaction (:reset-password-form @db))))

(defmethod layout/pages :reset-password [_]
  (let [reset-password-form (re-frame/subscribe [:reset-password-form])]
    (fn []
      [:div
       [:h1 "Reset Password"]
       [forms/form @reset-password-form (:-errors @reset-password-form) :update-reset-password-form
        [:form.form-horizontal {:on-submit #(ui/dispatch % [:reset-password])}
         [:div.col-sm-offset-2.col-sm-10 {:free-form/error-message {:key :-general}} [:p.text-danger]]
         [:div.form-group {:free-form/error-class {:key :email :error "has-error"}}
          [:label.col-sm-2.control-label {:for :email} "Email"]
          [:div.col-sm-10 [:input.form-control {:free-form/field {:key :email}
                                                :type            :email
                                                :id              :email
                                                :placeholder     "sam@example.com"}]
           [:div.text-danger {:free-form/error-message {:key :email}} [:p]]]]
         [:div.form-group
          [:div.col-sm-offset-2.col-sm-10
           [:button.btn.btn-primary {:type :submit :disabled (:-processing @reset-password-form)}
            (if (:-processing @reset-password-form)
              "Resetting password, please wait..."
              "Reset Password")]]]]]])))

(re-frame/register-handler
  :update-change-password-form
  (fn [db [_ ks value]]
    (let [db (assoc-in db (cons :change-password-form ks) value)]
      (if (nil? (get-in db [:change-password-form :-errors]))
        db
        (assoc-in db [:change-password-form :-errors] (user-schema/change-password-validation-by-token (:change-password-form db)))))))

(re-frame/register-handler
  :change-password
  (fn [db [_ html-form]]
    (let [change-password-form (:change-password-form db)
          current-user (:current-user db)]
      (if (validateur/valid? (if current-user
                               user-schema/change-password-validation-by-password
                               user-schema/change-password-validation-by-token)
                             change-password-form)
        (do (ajax/POST "/api/v1/change-password"
                       {:params        (-> (if current-user
                                             change-password-form
                                             (assoc change-password-form :token (get-in db [:current-route :url :query "token"])))
                                           (dissoc :-errors))
                        :handler       #(re-frame/dispatch [:got-change-password html-form (clojure.walk/keywordize-keys %1)])
                        :error-handler (fn [error]
                                         (re-frame/dispatch [:clean-up-processing [:change-password-form]])
                                         (util/report-unexpected-error error))})
            (assoc-in db [:change-password-form :-processing] true))
        (assoc-in db [:change-password-form :-errors] (user-schema/change-password-validation-by-token change-password-form))))))

(re-frame/register-handler
  :got-change-password
  (fn [db [_ html-form {status :status change-password-form :change-password-form}]]
    (if (= status "success")
      (do
        (if (:current-user db)
          (.reset html-form)
          (routing/redirect-to :log-in))
        (-> db
            (assoc :change-password-form {})
            (alerts/add-alert :success
                              (if (:current-user db)
                                "Your password was succesfully changed."
                                "Your password has been changed, you can now try logging in."))))
      (assoc db :change-password-form change-password-form))))

(re-frame/register-sub
  :change-password-form
  (fn [db _]
    (ratom/reaction (:change-password-form @db))))

(defmethod layout/pages :change-password [_]
  (let [change-password-form (re-frame/subscribe [:change-password-form])
        current-password (re-frame/subscribe [:current-user])]
    (fn []
      [:div
       [:h1 "Change Password"]
       [forms/form @change-password-form (:-errors @change-password-form) :update-change-password-form
        [:form.form-horizontal {:on-submit #(ui/dispatch % [:change-password])}
         [:div.col-sm-offset-2.col-sm-10 {:free-form/error-message {:key :-general}} [:p.text-danger]]
         (when @current-password
           [:div.form-group {:free-form/error-class {:ks [:current-password] :error "has-error"}}
            [:label.col-sm-2.control-label {:for :current-password} "Current Password"]
            [:div.col-sm-10 [:input.form-control {:free-form/field {:ks [:current-password]}
                                                  :type            :password
                                                  :id              :current-password}]
             [:div.text-danger {:free-form/error-message {:ks [:current-password]}} [:p]]]])
         [:div.form-group {:free-form/error-class {:ks [:password] :error "has-error"}}
          [:label.col-sm-2.control-label {:for :password} "New Password"]
          [:div.col-sm-10 [:input.form-control {:free-form/field {:ks [:password]}
                                                :type            :password
                                                :id              :password}]
           [:div.text-danger {:free-form/error-message {:ks [:password]}} [:p]]]]
         [:div.form-group {:free-form/error-class {:key :password-confirmation :error "has-error"}}
          [:label.col-sm-2.control-label {:for :password-confirmation} "Password Confirmation"]
          [:div.col-sm-10 [:input.form-control {:free-form/field {:key :password-confirmation}
                                                :type            :password
                                                :id              :password-confirmation}]
           [:div.text-danger {:free-form/error-message {:key :password-confirmation}} [:p]]]]
         [:div.form-group
          [:div.col-sm-offset-2.col-sm-10
           [:button.btn.btn-primary {:type :submit :disabled (:-processing @change-password-form)}
            (if (:-processing @change-password-form)
              "Changing Password, please wait..."
              "Change Password")]]]]]])))

(re-frame/register-handler
  :clean-up-processing
  (fn [db [_ ks]]
    (dissoc-in db ks)))
