;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.util
  (:require
    #?(:clj [yeller.clojure.client :as yeller])
    #?(:clj [yeller.clojure.ring :as yeller-ring])
    #?(:clj [environ.core :refer [env]])))

(defn println-ret [val]
  (println val)
  val)

#?(:clj
   (def yeller-options {:token       (:yeller-token env)
                        :environment (:environment env)}))

#?(:clj
   (def yeller-client (yeller/client yeller-options)))

#?(:clj
   (defn report-error
     ([exception] (report-error exception {}))
     ([exception extra]
      (when (not (or (:dev env) (:test env)))
        (let [exception (if (instance? String exception)
                          (Exception. exception)
                          exception)]
          (yeller/report yeller-client exception extra))))))

#?(:clj
   (defn format-request-for-error [request]
     (yeller-ring/format-extra yeller-options request)))

#?(:cljs
   (defn log [& args]
     (.log js/console (pr-str args))))

#?(:cljs
   (defn report-unexpected-error [{:keys [status status-text]}]
     (js/alert "We are sorry, there was an unexpected error.")
     (log "Error: " status status-text)))

; dissoc-in copied from clojure.incubator because it's clj only there, not cljc.
; https://github.com/clojure/core.incubator/blob/98672b9f627631215b47744fe193550f389d1095/src/main/clojure/clojure/core/incubator.clj#L62
(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure. keys is a sequence of keys. Any empty maps that result
  will not be present in the new structure."
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))
