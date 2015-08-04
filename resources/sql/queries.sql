-- name: delete-all!
-- deletes everything in the database
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

-- name: update-user!
-- update an existing user record
UPDATE users
SET first_name = :first_name, last_name = :last_name, email = :email
WHERE id = :id

-- name: get-user
-- retrieve a user given the id.
SELECT * FROM users
WHERE id = :id

-- name: delete-user!
-- delete a user given the id
DELETE FROM users
WHERE id = :id
