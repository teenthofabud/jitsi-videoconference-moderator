ALTER TABLE google_conference
DROP COLUMN organizer_email,
ADD COLUMN user_id BIGINT NOT NULL;