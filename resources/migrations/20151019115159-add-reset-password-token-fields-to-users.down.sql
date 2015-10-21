-- Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

ALTER TABLE users
DELETE COLUMN reset_password_token,
DELETE COLUMN reset_password_token_expires_at
