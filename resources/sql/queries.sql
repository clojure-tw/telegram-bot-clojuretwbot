-- name: add-url!
INSERT INTO tweets (url)
VALUES (:url)

-- name: find-url
SELECT * FROM tweets WHERE url = :url
