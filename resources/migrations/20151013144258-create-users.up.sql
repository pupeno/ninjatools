-- Copyright © 2015 Carousel Apps, Ltd. All rights reserved

CREATE TABLE users
(id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 email VARCHAR(254),
 password VARCHAR(200),
 name VARCHAR(300),
 created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'utc'),
 updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'utc'));
--;;
CREATE UNIQUE INDEX users_id ON users (id);
--;;
CREATE UNIQUE INDEX users_email ON users (email);


