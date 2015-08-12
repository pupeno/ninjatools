;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [ninjatools.db.core :as db]
            [ninjatools.models.tool :as tool]))

(s/defschema Thingie {:id    Long
                      :hot   Boolean
                      :tag   (s/enum :kikka :kukka)
                      :chief [{:name String
                               :type #{{:id String}}}]})

(defapi service-routes
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
