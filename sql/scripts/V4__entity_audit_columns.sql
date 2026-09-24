-- Backfill audit metadata for existing rows before enforcing NOT NULL.
ALTER TABLE principal
    ADD COLUMN version bigint,
    ADD COLUMN last_modified_date timestamp with time zone;

UPDATE principal
SET version = 0,
    last_modified_date = now()
WHERE version IS NULL OR last_modified_date IS NULL;

ALTER TABLE principal
    ALTER COLUMN version SET DEFAULT 0,
    ALTER COLUMN version SET NOT NULL,
    ALTER COLUMN last_modified_date SET DEFAULT now(),
    ALTER COLUMN last_modified_date SET NOT NULL;

ALTER TABLE server
    ADD COLUMN version bigint,
    ADD COLUMN last_modified_date timestamp with time zone;

UPDATE server
SET version = 0,
    last_modified_date = now()
WHERE version IS NULL OR last_modified_date IS NULL;

ALTER TABLE server
    ALTER COLUMN version SET DEFAULT 0,
    ALTER COLUMN version SET NOT NULL,
    ALTER COLUMN last_modified_date SET DEFAULT now(),
    ALTER COLUMN last_modified_date SET NOT NULL;

ALTER TABLE google_conference
    ADD COLUMN version bigint,
    ADD COLUMN last_modified_date timestamp with time zone;

UPDATE google_conference
SET version = 0,
    last_modified_date = now()
WHERE version IS NULL OR last_modified_date IS NULL;

ALTER TABLE google_conference
    ALTER COLUMN version SET DEFAULT 0,
    ALTER COLUMN version SET NOT NULL,
    ALTER COLUMN last_modified_date SET DEFAULT now(),
    ALTER COLUMN last_modified_date SET NOT NULL;
