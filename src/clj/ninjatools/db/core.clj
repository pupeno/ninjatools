;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.db.core
  (:require
    [cheshire.core :refer [generate-string parse-string]]
    [taoensso.timbre :as timbre]
    [clojure.java.jdbc :as jdbc]
    [conman.core :as conman]
    [to-jdbc-uri.core :refer [to-jdbc-uri]]
    [environ.core :refer [env]])
  (:import org.postgresql.util.PGobject
           org.postgresql.jdbc4.Jdbc4Array
           clojure.lang.IPersistentMap
           clojure.lang.IPersistentVector
           [java.sql BatchUpdateException
                     Date
                     Timestamp
                     PreparedStatement]))

(defonce ^:dynamic conn (atom nil))

; Generate query functions but wrap them around a conversion for the keys, so that they go from snake_case to kebab-case.
(ns ninjatools.db.core.queries
  (:require [conman.core :as conman]
            [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :as csk-extras]))
(doall (for [yesql-query (conman/bind-connection ninjatools.db.core/conn "sql/queries.sql")]
         (intern 'ninjatools.db.core
                 (with-meta (:name (meta yesql-query)) (meta yesql-query))
                 (fn [& args]
                   (let [args (if (< 1 (count args))
                                args
                                (cons (csk-extras/transform-keys csk/->snake_case (first args)) (rest args)))]
                     (csk-extras/transform-keys csk/->kebab-case (apply yesql-query args)))))))
(in-ns 'ninjatools.db.core)

(def pool-spec
  {:adapter    :postgresql
   :init-size  1
   :min-idle   1
   :max-idle   4
   :max-active 32})

(defn connect! []
  (conman/connect! conn (assoc pool-spec :jdbc-url (to-jdbc-uri (env :database-url)))))


(defn disconnect! []
  (conman/disconnect! conn))

(defn delete-all! []
  (delete-all-integrations!)
  (delete-all-tools!))

(defn get-tool-by-name [name]
  (first (get-tool-by-name- {:name name})))

(defn get-user-by-email [email]
  (first (get-user-by-email- {:email email})))

(defn get-user-by-id [id]
  (first (get-user-by-id- {:id id})))

(defn get-user-by-reset-password-token [token]
  (first (get-user-by-reset-password-token- {:token token})))

(defn to-date [sql-date]
  (-> sql-date (.getTime) (java.util.Date.)))

(extend-protocol jdbc/IResultSetReadColumn
  Date
  (result-set-read-column [v _ _] (to-date v))

  Timestamp
  (result-set-read-column [v _ _] (to-date v))

  Jdbc4Array
  (result-set-read-column [v _ _] (vec (.getArray v)))

  PGobject
  (result-set-read-column [pgobj _metadata _index]
    (let [type (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "json" (parse-string value true)
        "jsonb" (parse-string value true)
        "citext" (str value)
        value))))

(extend-type java.util.Date
  jdbc/ISQLParameter
  (set-parameter [v ^PreparedStatement stmt idx]
    (.setTimestamp stmt idx (Timestamp. (.getTime v)))))

(defn to-pg-json [value]
  (doto (PGobject.)
    (.setType "jsonb")
    (.setValue (generate-string value))))

(extend-protocol jdbc/ISQLValue
  IPersistentMap
  (sql-value [value] (to-pg-json value))
  IPersistentVector
  (sql-value [value] (to-pg-json value)))
