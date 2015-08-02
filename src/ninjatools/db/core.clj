;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.db.core
  (:require
    [cheshire.core :refer [generate-string parse-string]]
    ;[taoensso.timbre :as timbre]
    [clojure.java.jdbc :as jdbc]
    ;[yesql.core :as yesql]
    [luminus-db.core :as db]
    ;[clj-dbcp.core :as dbcp]
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

(db/bind-connection conn "sql/queries.sql")

(def pool-spec
  {:adapter    :postgresql
   :init-size  1
   :min-idle   1
   :max-idle   4
   :max-active 32})

(defn connect! []
  (db/connect! conn (assoc pool-spec :jdbc-url (to-jdbc-uri (env :database-url)))))

(defn disconnect! []
  (db/disconnect! conn))

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
