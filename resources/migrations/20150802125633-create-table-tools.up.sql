-- Copyright © 2015 Carousel Apps, Ltd. All rights reserved

CREATE TABLE tools
(id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 name VARCHAR(200) NOT NULL,
 slug VARCHAR(200) NOT NULL,
 url VARCHAR(500),
 description TEXT);
--;;
CREATE UNIQUE INDEX tools_id ON tools (id);
--;;
CREATE UNIQUE INDEX tools_name ON tools (name);
--;;
CREATE UNIQUE INDEX tools_slug ON tools (slug);