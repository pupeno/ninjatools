-- Copyright © 2015 Carousel Apps, Ltd. All rights reserved

CREATE TABLE features
(id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 name VARCHAR(200) NOT NULL,
 slug VARCHAR(200) NOT NULL,
 description TEXT);
--;;
CREATE UNIQUE INDEX features_id ON features (id);
--;;
CREATE UNIQUE INDEX features_name ON features (name);
--;;
CREATE UNIQUE INDEX features_slug ON features (slug);