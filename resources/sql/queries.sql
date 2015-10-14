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
WHERE email = :email
