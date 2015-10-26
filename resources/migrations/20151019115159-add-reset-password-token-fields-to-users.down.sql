-- Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

ALTER TABLE users
DROP COLUMN reset_password_token,
DROP COLUMN reset_password_token_expires_at
