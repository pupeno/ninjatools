-- Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

ALTER TABLE users
DROP CONSTRAINT users_reset_password_token
--;;
CREATE UNIQUE INDEX users_reset_password_token ON users (reset_password_token);
--;;
CREATE UNIQUE INDEX users_id ON users (id);
--;;
ALTER TABLE used_tools
DROP CONSTRAINT used_tools_user_id_fkey,
DROP CONSTRAINT used_tools_tool_id_fkey,
DROP CONSTRAINT used_tools_user_id_tool_id -- Not bothering to change default and types as it won't prevent rolling back and it has almost no effect.
--;;
CREATE UNIQUE INDEX used_tools_user_id_tool_id ON used_tools (user_id, tool_id);
--;;
ALTER TABLE tools_features
DROP CONSTRAINT tools_features_tool_id_feature_id -- Not bothering to change default and types as it won't prevent rolling back and it has almost no effect.
--;;
CREATE UNIQUE INDEX tools_features_id ON tools_features (id)
--;;
CREATE UNIQUE INDEX tools_features_tool_id_feature_id ON tools_features (tool_id, feature_id)
--;;
ALTER TABLE tools
DROP CONSTRAINT tools_name,
DROP CONSTRAINT tools_slug -- Not bothering to change default and types as it won't prevent rolling back and it has almost no effect.
--;;
CREATE UNIQUE INDEX tools_id ON tools (id)
--;;
CREATE UNIQUE INDEX tools_name ON tools (name)
--;;
CREATE UNIQUE INDEX tools_slug ON tools (slug)
--;;
ALTER TABLE integrations
DROP CONSTRAINT integrations_tool_a_id_tool_b_id -- Not bothering to change default and types as it won't prevent rolling back and it has almost no effect.
--;;
CREATE UNIQUE INDEX integrations_tool_a_id_tool_b_id ON integrations (tool_a_id, tool_b_id)
--;;
CREATE UNIQUE INDEX integrations_id ON integrations (id)
--;;
ALTER TABLE features
DROP CONSTRAINT features_name,
DROP CONSTRAINT features_slug -- Not bothering to change default and types as it won't prevent rolling back and it has almost no effect.
--;;
CREATE UNIQUE INDEX features_slug ON features (slug)
--;;
CREATE UNIQUE INDEX features_name ON features (name)
--;;
CREATE UNIQUE INDEX features_id ON features (id)
