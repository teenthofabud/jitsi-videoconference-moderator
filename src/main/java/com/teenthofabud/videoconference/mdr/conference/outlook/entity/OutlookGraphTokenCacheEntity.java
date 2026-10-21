package com.teenthofabud.videoconference.mdr.conference.outlook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;

@Entity
@Table(name = "outlook_graph_token_cache")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor
public class OutlookGraphTokenCacheEntity {
    @Id @Column(name = "user_id") private Long userId;
    @Column(name = "microsoft_user_id") private String microsoftUserId;
    @Column(name = "mailbox_email") private String mailboxEmail;
    @Column(name = "tenant_id") private String tenantId;
    @Column(name = "access_token_encrypted", columnDefinition = "text") private String accessTokenEncrypted;
    @Column(name = "refresh_token_encrypted", columnDefinition = "text") private String refreshTokenEncrypted;
    @Column(name = "access_token_expires_at") private Instant accessTokenExpiresAt;
    @Column(name = "subscription_id") private String subscriptionId;
    @Column(name = "subscription_expires_at") private Instant subscriptionExpiresAt;
    @Column(name = "subscription_client_state") private String subscriptionClientState;
    @Column(name = "pending_state_hash") private String pendingStateHash;
    @Column(name = "pending_code_verifier", columnDefinition = "text") private String pendingCodeVerifier;
    @Column(name = "pending_expires_at") private Instant pendingExpiresAt;
    @Column(name = "connection_status", nullable = false) private String connectionStatus = "DISCONNECTED";
    @Column(name = "last_sync_error", columnDefinition = "text") private String lastSyncError;
    @Column(name = "reconciliation_required", nullable = false) private boolean reconciliationRequired;
    @Version @Column(nullable = false) private Long version;
    @LastModifiedDate @Column(name = "last_modified_date", nullable = false) private Instant lastModifiedDate;
}
