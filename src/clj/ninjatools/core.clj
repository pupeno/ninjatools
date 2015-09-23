;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.core
  (:require [ninjatools.handler :refer [app init destroy parse-port]]
            ninjatools.layout
            [immutant.web :as immutant]
            [prerenderer.core :as prerenderer]
            [ninjatools.db.migrations :as migrations]
            [taoensso.timbre :as timbre]
            [environ.core :refer [env]])
  (:gen-class))

(defonce server (atom nil))

(defn http-port [port]
  (parse-port (or port (env :port) 3000)))

(defn start-server [port]
  (init)
  (reset! server (immutant/run app
                               :host (if (env :production) "0.0.0.0" "localhost")
                               :port port))
  (reset! ninjatools.layout/js-engine (prerenderer/run {:path              "target/js/server-side.js"
                                                        :default-ajax-port port
                                                        :wait              (env :dev)})))

(defn stop-server []
  (when @server
    (destroy)
    (immutant/stop @server)
    (reset! server nil)))

(defn start-app [[port]]
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-server))
  (start-server (http-port port))
  (timbre/info "server started on port:" (str (:host @server) ":" (:port @server))))

(defn -main [& args]
  (cond
    (some #{"migrate" "rollback"} args) (migrations/migrate args)
    :else (start-app args)))
  
