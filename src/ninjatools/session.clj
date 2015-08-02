;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.session
  (:require [ring-ttl-session.core :refer [ttl-memory-store]]))

(defonce ttl-mem (ttl-memory-store (* 60 30)))
