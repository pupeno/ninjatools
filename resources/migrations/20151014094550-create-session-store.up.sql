-- Copyright © 2015 Carousel Apps, Ltd. All rights reserved

CREATE TABLE session_store
(session_id VARCHAR(36) NOT NULL PRIMARY KEY,
 idle_timeout BIGINT,
 absolute_timeout BIGINT,
 value BYTEA)
