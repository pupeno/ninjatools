-- Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

ALTER TABLE users
ADD COLUMN reset_password_token UUID,
ADD COLUMN reset_password_token_expires_at TIMESTAMP(6)
--;;
CREATE UNIQUE INDEX users_reset_password_token ON users (reset_password_token);
