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
