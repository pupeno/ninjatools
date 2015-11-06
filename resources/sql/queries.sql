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
