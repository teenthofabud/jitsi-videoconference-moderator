CREATE SCHEMA IF NOT EXISTS jitsi_moderator;

-- Flyway start --

-- Create Migrator Role
DO $$ 
BEGIN
  CREATE ROLE moderator_schema_migrator;
EXCEPTION WHEN duplicate_object THEN
  RAISE NOTICE 'Role moderator_schema_migrator already exists, skipping...';
END $$;

GRANT ALL PRIVILEGES ON SCHEMA jitsi_moderator TO moderator_schema_migrator;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA jitsi_moderator TO moderator_schema_migrator;
ALTER DEFAULT PRIVILEGES IN SCHEMA jitsi_moderator GRANT ALL PRIVILEGES ON TABLES TO moderator_schema_migrator;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA jitsi_moderator TO moderator_schema_migrator;
ALTER DEFAULT PRIVILEGES IN SCHEMA jitsi_moderator GRANT ALL PRIVILEGES ON SEQUENCES TO moderator_schema_migrator;

-- Create Flyway migration user
DO $$ 
BEGIN
  CREATE USER app_moderator_flyway LOGIN PASSWORD 'H3daMqf8wwrN';
EXCEPTION WHEN duplicate_object THEN
  RAISE NOTICE 'User app_moderator_flyway already exists, skipping...';
END $$;

-- Assign role
GRANT moderator_schema_migrator TO app_moderator_flyway;

-- Flyway end --

-- Server start --

-- Create Server Manager Role
DO $$
BEGIN
  CREATE ROLE moderator_server_manager;
EXCEPTION WHEN duplicate_object THEN
  RAISE NOTICE 'Role moderator_server_manager already exists, skipping...';
END $$;

-- Grant DML (Read/Write) privileges on CURRENT objects
GRANT USAGE ON SCHEMA jitsi_moderator TO moderator_server_manager;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA jitsi_moderator TO moderator_server_manager;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA jitsi_moderator TO moderator_server_manager;

-- Grant DML (Read/Write) privileges on FUTURE objects created by Flyway
ALTER DEFAULT PRIVILEGES FOR ROLE moderator_schema_migrator IN SCHEMA jitsi_moderator
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO moderator_server_manager;

ALTER DEFAULT PRIVILEGES FOR ROLE moderator_schema_migrator IN SCHEMA jitsi_moderator
GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO moderator_server_manager;

-- Create the application user
DO $$
BEGIN
  CREATE USER app_jitsi_moderator_service_user LOGIN PASSWORD 'RJHGHR4efjhhrj7defhe';
EXCEPTION WHEN duplicate_object THEN
  RAISE NOTICE 'User app_jitsi_moderator_service_user already exists, skipping...';
END $$;

-- Assign the DML role to the application user
GRANT moderator_server_manager TO app_jitsi_moderator_service_user;

-- Server end --