;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [environ.core :refer [env]]
            [ninjatools.db.core :as db]
            [ninjatools.models.tool :as tool]
            [ninjatools.models.user :as user]
            [ninjatools.models.user-schema :as user-schema]
            [validateur.validation :as validateur]
            [clojurewerkz.mailer.core :as mailer]
            [yeller.clojure.client :as yeller]))

(def yeller-client (yeller/client {:token       (:yeller-token env) ; TODO: move to a more generic place when we need to report errors from more than one place.
                                   :environment (:environment env)}))

#_(s/defschema Thingie {:id    Long
                        :hot   Boolean
                        :tag   (s/enum :kikka :kukka)
                        :chief [{:name String
                                 :type #{{:id String}}}]})

(defapi service-routes
        ; Report errors to Yeller. Read more here: https://github.com/metosin/compojure-api#exception-handling and here: http://docs.yellerapp.com/platforms/clojure/getting_started.html
        {:exceptions {:handlers {:compojure.api.exception/response-validation (fn [^Exception e data request]
                                                                                (when (not (or (:dev env) (:test env)))
                                                                                  (yeller/report yeller-client e {:data data :request request}))
                                                                                (compojure.api.exception/response-validation-handler e data request))
                                 :compojure.api.exception/default             (fn [^Exception e data request]
                                                                                (when (not (or (:dev env) (:test env)))
                                                                                  (yeller/report yeller-client e {:data data :request request}))
                                                                                (compojure.api.exception/safe-handler e data request))}}}
        (ring.swagger.ui/swagger-ui "/api")
        ;JSON docs available at the /swagger.json route
        (swagger-docs {:info {:title "Ninja Tools API"}})
        (context* "/api/v1" []
                  :tags ["v1"]
                  (GET* "/tools" []
                        :summary "Return all the tools."
                        (ok (db/get-tools)))
                  (GET* "/tools/:id/integrations" []
                        :summary "Return all the integrated tools to a given tool."
                        :path-params [id :- s/Uuid]
                        (ok (tool/get-integrations-for id)))

                  (GET* "/current-user" {current-user :current-user}
                        (if current-user
                          (ok (user/sanitize-for-public current-user))
                          (ok)))
                  (PUT* "/log-out" {session :session}
                        (assoc (ok) :session (dissoc session :identity)))
                  (PUT* "/log-in" {session :session}
                        :summary "Log in as a user"
                        :body [log-in-form user-schema/LogInSchema]
                        ; TODO: :return
                        (if (validateur/valid? user-schema/log-in-validation log-in-form)
                          (if-let [user (user/get-by-credentials log-in-form)]
                            (-> (ok {:status :success :user (user/sanitize-for-public user)})
                                (assoc :session (assoc session :identity (:id user))))
                            (ok {:status :failed :log-in-form (update-in log-in-form [:-errors :-general] #(conj (or %1 []) "Email and password doesn't match"))}))
                          (ok {:status :failed :log-in-form (assoc log-in-form :-errors (user-schema/log-in-validation log-in-form))})))

                  (POST* "/register" {session :session}
                         :summary "Register as a new user"
                         :body [registration-form user-schema/RegistrationSchema]
                         ; TODO: :return {:status (s/enum :success :failed)
                         ;        :registration user-schema/RegistrationValidationSchema}
                         (if (validateur/valid? user/registration-validation registration-form)
                           (let [user (user/create (dissoc registration-form :password-confirmation))]
                             (-> (ok {:status :success :user (user/sanitize-for-public user)})
                                 (assoc :session (assoc session :identity (:id user)))))
                           (ok {:status :failed :registration-form (assoc registration-form :-errors (user/registration-validation registration-form))})))

                  (POST* "/reset-password" []
                         :summary "Reset the password of an existing account"
                         :body [reset-password-form user-schema/ResetPasswordSchema]
                         ; TODO: return
                         (if (validateur/valid? user-schema/reset-password-validation reset-password-form)
                           (let [user (db/get-user-by-email (:email reset-password-form))]
                             (when user
                               (let [user (db/generate-reset-password-token<! user)
                                     template-vars {:name             (user-schema/display-name user)
                                                    :email            (:email user)
                                                    :set-password-url (str (:email-url env) "change-password?token=" (:reset-password-token user))}]
                                 (mailer/deliver-email {:to      (str (user-schema/display-name user) " <" (:email user) ">")
                                                        :subject "Reset your password for Ninja Tools"}
                                                       "templates/email/reset-password.txt.mustache" template-vars :text/plain
                                                       "templates/email/reset-password.html.mustache" template-vars :text/html)))
                             (ok {:status :success :reset-password-form reset-password-form}))
                           (ok {:status :failed :reset-password-form (assoc reset-password-form :-errors (user-schema/reset-password-validation reset-password-form))})))

                  (PUT* "/change-password" {current-user :current-user}
                        :summary "Change the password for the logged in user or the specified token"
                        :body [change-password-form user-schema/ChangePasswordSchema]
                        ; TODO: return
                        (if current-user
                          (if (validateur/valid? user-schema/change-password-validation-by-password change-password-form)
                            (if (user/check-password current-user (:current-password change-password-form))
                              (do (user/update-password current-user (:password change-password-form))
                                  (ok {:status :success}))
                              (ok {:status :failed :change-password-form (assoc-in change-password-form [:-errors :current-password] ["Your password doesn't match."])}))
                            (ok {:status :failed :change-password-form (assoc change-password-form :-errors (user-schema/change-password-validation-by-token change-password-form))}))
                          (if (validateur/valid? user-schema/change-password-validation-by-token change-password-form)
                            (if-let [current-user (db/get-user-by-reset-password-token (:token change-password-form))]
                              (do (user/update-password current-user (:password change-password-form))
                                  (ok {:status :success}))
                              (ok {:status :failed :change-password-form (assoc-in change-password-form [:-errors :-general] ["The reset token seems to be invalid or out of date. Please, start the reset password process again."])}))
                            (ok {:status :failed :change-password-form (assoc change-password-form :-errors (user-schema/change-password-validation-by-token change-password-form))}))))

                  (GET* "/fail" []
                        :summary "Test error reporting"
                        (throw (Exception. "Bogus error to test exception handling on the server, api.")))

                  #_(GET* "/plus" []
                          :return Long
                          :query-params [x :- Long, {y :- Long 1}]
                          :summary "x+y with query-parameters. y defaults to 1."
                          (ok (+ x y)))

                  #_(POST* "/minus" []
                           :return Long
                           :body-params [x :- Long, y :- Long]
                           :summary "x-y with body-parameters."
                           (ok (- x y)))

                  #_(GET* "/times/:x/:y" []
                          :return Long
                          :path-params [x :- Long, y :- Long]
                          :summary "x*y with path-parameters"
                          (ok (* x y)))

                  #_(POST* "/divide" []
                           :return Double
                           :form-params [x :- Long, y :- Long]
                           :summary "x/y with form-parameters"
                           (ok (/ x y)))

                  #_(GET* "/power" []
                          :return Long
                          :header-params [x :- Long, y :- Long]
                          :summary "x^y with header-parameters"
                          (ok (long (Math/pow x y))))

                  #_(PUT* "/echo" []
                          :return [{:hot Boolean}]
                          :body [body [{:hot Boolean}]]
                          :summary "echoes a vector of anonymous hotties"
                          (ok body))

                  #_(POST* "/echo" []
                           :return (s/maybe Thingie)
                           :body [thingie (s/maybe Thingie)]
                           :summary "echoes a Thingie from json-body"
                           (ok thingie)))

        #_(context* "/context" []
                    :tags ["context*"]
                    :summary "summary inherited from context"
                    (context* "/:kikka" []
                              :path-params [kikka :- s/Str]
                              :query-params [kukka :- s/Str]
                              (GET* "/:kakka" []
                                    :path-params [kakka :- s/Str]
                                    (ok {:kikka kikka
                                         :kukka kukka
                                         :kakka kakka})))))
