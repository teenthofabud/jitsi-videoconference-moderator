CREATE TABLE outlook_graph_token_cache (
    user_id bigint PRIMARY KEY,
    microsoft_user_id varchar(255),
    mailbox_email varchar(320),
    tenant_id varchar(255),
    access_token_encrypted text,
    refresh_token_encrypted text,
    access_token_expires_at timestamp with time zone,
    subscription_id varchar(255),
    subscription_expires_at timestamp with time zone,
    subscription_client_state varchar(255),
    pending_state_hash varchar(255),
    pending_code_verifier text,
    pending_expires_at timestamp with time zone,
    connection_status varchar(32) NOT NULL DEFAULT 'DISCONNECTED'
        CHECK (connection_status IN ('DISCONNECTED', 'CONNECTED', 'SUBSCRIPTION_ERROR', 'REAUTH_REQUIRED')),
    last_sync_error text,
    reconciliation_required boolean NOT NULL DEFAULT false,
    version bigint NOT NULL DEFAULT 0,
    last_modified_date timestamp with time zone NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX outlook_graph_token_cache_subscription_id_uq
    ON outlook_graph_token_cache (subscription_id)
    WHERE subscription_id IS NOT NULL;
CREATE UNIQUE INDEX outlook_graph_token_cache_pending_state_hash_uq
    ON outlook_graph_token_cache (pending_state_hash)
    WHERE pending_state_hash IS NOT NULL;

CREATE INDEX outlook_graph_token_cache_renewal_idx
    ON outlook_graph_token_cache (subscription_expires_at)
    WHERE connection_status = 'CONNECTED';
CREATE INDEX outlook_graph_token_cache_reconciliation_idx
    ON outlook_graph_token_cache (user_id)
    WHERE reconciliation_required = true AND connection_status = 'CONNECTED';
