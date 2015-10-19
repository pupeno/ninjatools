-- Copyright © 2015 Carousel Apps, Ltd. All rights reserved

DROP INDEX users_email;
--;;
CREATE UNIQUE INDEX users_email ON users (lower(email));
