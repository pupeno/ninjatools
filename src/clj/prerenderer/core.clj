;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns prerenderer.core
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clj-http.client :as http]
            ring.util.request)
  (:import java.lang.ProcessBuilder
           java.io.File))

(defn create
  ([options]
   (let [engine (merge {:path              nil
                        :resource          nil
                        :wait-for-resource false
                        :process           nil
                        :port-file         (.getPath (doto (File/createTempFile (str "com.carouselapps.prerenderer-" *ns* "-") ".port")
                                                       .deleteOnExit))
                        :start-timeout     5000}
                       options)]
     (if (and (nil? (:path engine))
              (nil? (:resource engine)))
       (throw (Exception. "Either resource or path should be specified when creating an engine.")))
     (atom engine))))

(defn running? [engine]
  (let [process (:process @engine)]
    (and (not (nil? process))
         (.isAlive process))))

(defmacro with-timeout
  "Execute body until millis time happened.
  http://stackoverflow.com/questions/6694530/executing-a-function-with-a-timeout/6697469#6697469"
  [millis & body]
  `(let [future# (future ~@body)]
     (try
       (.get future# ~millis java.util.concurrent.TimeUnit/MILLISECONDS)
       (catch java.util.concurrent.TimeoutException x#
         (do
           (future-cancel future#)
           nil)))))

(defn read-port-file [engine]
  (slurp (:port-file engine)))

(defn wait-for-port-number [engine]
  (with-timeout
    (:start-timeout engine)
    (loop [port-number (read-port-file engine)]
      (if (string/blank? port-number)
        (if (not (.isAlive (:process engine)))
          (throw (Exception. (str "While waiting for port number, process died: " (:path engine))))
          (do (Thread/sleep 100)
              (recur (read-port-file engine))))
        port-number))))

(defn start-engine [engine]
  (println engine)
  (spit (:port-file engine) "")
  (let [process-builder (doto (ProcessBuilder. ["node" (:path engine) "--port-file" (:port-file engine)])
                          .inheritIO)
        process (.start process-builder)
        engine (assoc engine :process process)]
    (if-let [port-number (wait-for-port-number engine)]
      (assoc engine :port-number port-number)
      (throw (Exception. (str "Failed to get port number when starting " (:path engine)))))))

(defn resource-to-path [path wait]
  (let [full-path (io/resource path)
        error-message (str "Resource " path " for prerendering is not present. Did you compile your JavaScript? 'lein cljsbuild auto' maybe?")]
    (if (nil? full-path)
      (if wait
        (do
          (println error-message "Waiting until it appears")
          (Thread/sleep 100)
          (recur path wait))
        (throw (Exception. error-message)))
      (.getPath full-path))))

(defn resolve-path [engine]
  (if (nil? (:path engine))
    (assoc engine :path (resource-to-path (:resource engine) (:wait-for-resource engine)))
    engine))

(defn ensure-engine-is-running! [engine]
  (locking engine
    (when (not (running? engine))
      (reset! engine (merge @engine (start-engine (resolve-path @engine)))))))

(defn render [engine request]
  (ensure-engine-is-running! engine)
  (let [url (str "http://localhost:" (:port-number @engine) "/render?" (http/generate-query-string {:url (ring.util.request/request-url request)}))]
    (:body (http/get url {:headers (:headers request)}))))
