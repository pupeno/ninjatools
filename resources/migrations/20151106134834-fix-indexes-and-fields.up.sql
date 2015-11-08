-- Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

DROP INDEX features_id
--;;
DROP INDEX features_name
--;;
DROP INDEX features_slug
--;;
ALTER TABLE features
ADD CONSTRAINT features_name UNIQUE (name),
ADD CONSTRAINT features_slug UNIQUE (slug),
ALTER COLUMN created_at TYPE TIMESTAMP WITHOUT TIME ZONE,
ALTER COLUMN created_at SET DEFAULT (NOW() AT TIME ZONE 'utc'),
ALTER COLUMN updated_at TYPE TIMESTAMP WITHOUT TIME ZONE,
ALTER COLUMN updated_at SET DEFAULT (NOW() AT TIME ZONE 'utc')
--;;
DROP INDEX integrations_id
--;;
DROP INDEX integrations_tool_a_id_tool_b_id
--;;
ALTER TABLE integrations
ADD CONSTRAINT integrations_tool_a_id_tool_b_id UNIQUE (tool_a_id, tool_b_id),
ALTER COLUMN created_at TYPE TIMESTAMP WITHOUT TIME ZONE,
ALTER COLUMN created_at SET DEFAULT (NOW() AT TIME ZONE 'utc'),
ALTER COLUMN updated_at TYPE TIMESTAMP WITHOUT TIME ZONE,
ALTER COLUMN updated_at SET DEFAULT (NOW() AT TIME ZONE 'utc')
--;;
DROP INDEX tools_id
--;;
DROP INDEX tools_name
--;;
DROP INDEX tools_slug
--;;
ALTER TABLE tools
ADD CONSTRAINT tools_name UNIQUE (name),
ADD CONSTRAINT tools_slug UNIQUE (slug),
ALTER COLUMN created_at TYPE TIMESTAMP WITHOUT TIME ZONE,
ALTER COLUMN created_at SET DEFAULT (NOW() AT TIME ZONE 'utc'),
ALTER COLUMN updated_at TYPE TIMESTAMP WITHOUT TIME ZONE,
ALTER COLUMN updated_at SET DEFAULT (NOW() AT TIME ZONE 'utc')
--;;
DROP INDEX tools_features_id
--;;
DROP INDEX tools_features_tool_id_feature_id
--;;
ALTER TABLE tools_features
ADD CONSTRAINT tools_features_tool_id_feature_id UNIQUE (tool_id, feature_id),
ALTER COLUMN created_at TYPE TIMESTAMP WITHOUT TIME ZONE,
ALTER COLUMN created_at SET DEFAULT (NOW() AT TIME ZONE 'utc'),
ALTER COLUMN updated_at TYPE TIMESTAMP WITHOUT TIME ZONE,
ALTER COLUMN updated_at SET DEFAULT (NOW() AT TIME ZONE 'utc')
--;;
DROP INDEX used_tools_user_id_tool_id
--;;
ALTER TABLE used_tools
ADD CONSTRAINT used_tools_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id),
ADD CONSTRAINT used_tools_tool_id_fkey FOREIGN KEY (tool_id) REFERENCES tools (id),
ADD CONSTRAINT used_tools_user_id_tool_id UNIQUE (user_id, tool_id),
ALTER COLUMN created_at TYPE TIMESTAMP WITHOUT TIME ZONE,
ALTER COLUMN created_at SET DEFAULT (NOW() AT TIME ZONE 'utc'),
ALTER COLUMN updated_at TYPE TIMESTAMP WITHOUT TIME ZONE,
ALTER COLUMN updated_at SET DEFAULT (NOW() AT TIME ZONE 'utc')
--;;
DROP INDEX users_id
--;;
DROP INDEX users_reset_password_token
--;;
ALTER TABLE users
ADD CONSTRAINT users_reset_password_token UNIQUE (reset_password_token)
