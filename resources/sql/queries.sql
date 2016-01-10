-- name: delete-all-tools!
-- Delete all the tools from the database
DELETE FROM tools

-- name: get-tools
-- Get all the tools
SELECT *
FROM tools

-- name: create-tool<!
-- creates a new tool
INSERT INTO tools
(name, slug, url, description)
VALUES (:name, :slug, :url, :description)

-- name: get-tool-by-name-
-- Get a tool by its name
SELECT *
FROM tools
WHERE name = :name

-- name: delete-all-integrations!
-- Delete all the tools from the database
DELETE FROM integrations

-- name: create-integration<!
INSERT INTO integrations
(tool_a_id, tool_b_id, comment)
VALUES (:tool_a_id, :tool_b_id, :comment)

-- name: get-integrations-for
SELECT *
FROM integrations
WHERE tool_a_id = :tool_id
   OR tool_b_id = :tool_id

-- name: create-user<!
INSERT INTO users
(email, password)
VALUES (:email, :password)

-- name: get-user-by-id-
SELECT *
FROM users
WHERE id = :id

-- name: get-user-by-email-
SELECT *
FROM users
WHERE LOWER(email) = LOWER(:email)

-- name: get-user-by-reset-password-token-
SELECT *
FROM users
WHERE reset_password_token = :token
  AND reset_password_token_expires_at >= now()

-- name: generate-reset-password-token<!
UPDATE users
SET reset_password_token = uuid_generate_v4(),
    reset_password_token_expires_at = now() + interval '1' day,
    updated_at = now()
WHERE id = :id

-- name: update-password<!
UPDATE users
SET password = :password,
    reset_password_token = NULL,
    reset_password_token_expires_at = NULL
WHERE id = :id

-- name: get-used-tools
SELECT *
FROM tools
JOIN used_tools ON used_tools.tool_id = tools.id
WHERE used_tools.user_id = :user_id

-- name: add-used-tool<!
INSERT INTO used_tools
(user_id, tool_id)
VALUES (:user_id, :tool_id)

-- name: delete-used-tool!
DELETE FROM used_tools
WHERE user_id = :user_id
  AND tool_id = :tool_id

-- name: get-features
SELECT *
FROM features

-- name: get-wanted-features
SELECT *
FROM features
JOIN wanted_features ON wanted_features.feature_id = features.id
WHERE wanted_features.user_id = :user_id

-- name: add-wanted-feature<!
INSERT INTO wanted_features
(user_id, feature_id)
VALUES (:user_id, :feature_id)

-- name: delete-wanted-feature!
DELETE FROM wanted_features
WHERE user_id = :user_id
  AND feature_id = :feature_id

-- name: -get-tools-for-wanted-feature
-- Returns tools for a wanted feature (:feature_id), but only the ones that integrate with any of the used tools (:tool_ids)
SELECT tools.id, tools.name, features.name AS feature FROM tools
JOIN tools_features ON tools.id = tools_features.tool_id
JOIN features ON features.id = tools_features.feature_id AND features.id = :feature_id
LEFT JOIN integrations i1 ON tools.id = i1.tool_a_id AND i1.tool_b_id IN (:tool_ids)
LEFT JOIN integrations i2 ON tools.id = i2.tool_b_id AND i2.tool_a_id IN (:tool_ids)
WHERE (i1.tool_b_id IS NOT NULL OR i2.tool_a_id IS NOT NULL)