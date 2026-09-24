ALTER TABLE principal
    ADD COLUMN version bigint NOT NULL DEFAULT 0,
    ADD COLUMN last_modified_date timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE server
    ADD COLUMN version bigint NOT NULL DEFAULT 0,
    ADD COLUMN last_modified_date timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE google_conference
    ADD COLUMN version bigint NOT NULL DEFAULT 0,
    ADD COLUMN last_modified_date timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP;
