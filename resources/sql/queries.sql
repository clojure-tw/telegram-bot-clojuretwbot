-- name: create-url!
-- creates a new url record
INSERT INTO tweets (url)
VALUES (:url)

-- name: get-url
-- retrieve a tweets given the url.
SELECT * FROM tweets
WHERE url = :url

-- name: delete-url
-- delete a tweets given the url.
DELETE * FROM tweets
WHERE url = :url

-- name: list-all-url
-- list all url from tweets
SELECT * from tweets;