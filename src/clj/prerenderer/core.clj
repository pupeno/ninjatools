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
   (let [engine (merge {:path          nil
                        :wait          false
                        :process       nil
                        :port-file     (.getPath (doto (File/createTempFile (str "com.carouselapps.prerenderer-" *ns* "-") ".port")
                                                   .deleteOnExit))
                        :start-timeout 5000}
                       options)]
     (if (nil? (:path engine))
       (throw (Exception. "Path should be specified when creating an engine.")))
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

(defn ensure-javascript-exists
  ([engine] (ensure-javascript-exists engine false))
  ([engine notify-about-file-appearing]
   (if (not (.exists (io/as-file (:path engine))))
     (let [message (str "File " (:path engine) " for prerendering is not present. Did you compile your JavaScript? 'lein cljsbuild auto' maybe?")]
       (if (:wait engine)
         (do
           (println message "Waiting until it appears...")
           (Thread/sleep 100)
           (recur engine true))
         (throw (Exception. message))))
     (when notify-about-file-appearing
       (println "File" (:path engine) "appeared. Pfiuuu!")))))

(defn start-engine [engine]
  (spit (:port-file engine) "")
  (ensure-javascript-exists engine)
  (let [process-builder (doto (ProcessBuilder. ["node" (:path engine) "--port-file" (:port-file engine)])
                          .inheritIO)
        process (.start process-builder)
        engine (assoc engine :process process)]
    (if-let [port-number (wait-for-port-number engine)]
      (assoc engine :port-number port-number)
      (throw (Exception. (str "Failed to get port number when starting " (:path engine)))))))

(defn ensure-engine-is-running! [engine]
  (locking engine
    (when (not (running? engine))
      (reset! engine (start-engine @engine)))))

(defn render [engine request]
  (ensure-engine-is-running! engine)
  (let [url (str "http://localhost:" (:port-number @engine) "/render?" (http/generate-query-string {:url (ring.util.request/request-url request)}))]
    (:body (http/get url {:headers (:headers request)}))))
