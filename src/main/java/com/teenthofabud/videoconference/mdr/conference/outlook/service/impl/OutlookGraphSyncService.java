package com.teenthofabud.videoconference.mdr.conference.outlook.service.impl;

import com.teenthofabud.videoconference.mdr.configuration.outlook.OutlookGraphProperties;
import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import com.teenthofabud.videoconference.mdr.conference.outlook.client.OutlookGraphClient;
import com.teenthofabud.videoconference.mdr.conference.outlook.client.OutlookGraphRequestException;
import com.teenthofabud.videoconference.mdr.conference.outlook.dto.OutlookGraphDtos.Event;
import com.teenthofabud.videoconference.mdr.conference.outlook.dto.OutlookGraphDtos.Notification;
import com.teenthofabud.videoconference.mdr.conference.outlook.dto.OutlookGraphDtos.NotificationPayload;
import com.teenthofabud.videoconference.mdr.conference.outlook.dto.OutlookGraphDtos.Subscription;
import com.teenthofabud.videoconference.mdr.conference.outlook.dto.OutlookGraphDtos.TokenResponse;
import com.teenthofabud.videoconference.mdr.conference.outlook.dto.OutlookGraphDtos.UserProfile;
import com.teenthofabud.videoconference.mdr.conference.outlook.entity.OutlookGraphConnectionEntity;
import com.teenthofabud.videoconference.mdr.conference.outlook.repository.OutlookConferenceRepository;
import com.teenthofabud.videoconference.mdr.conference.outlook.repository.OutlookGraphConnectionRepository;
import com.teenthofabud.videoconference.mdr.conference.outlook.util.OutlookGraphConfigurationValidator;
import com.teenthofabud.videoconference.mdr.conference.outlook.util.OutlookGraphUrlBuilder;
import com.teenthofabud.videoconference.mdr.conference.outlook.util.OutlookOAuthResultPageBuilder;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static com.teenthofabud.videoconference.mdr.conference.outlook.util.OutlookGraphUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutlookGraphSyncService {
    private static final String STATUS_CONNECTED = "CONNECTED";

    private final OutlookGraphProperties properties;
    private final OutlookGraphConnectionRepository connectionRepository;
    private final OutlookConferenceRepository conferenceRepository;
    private final UserService userService;
    private final OutlookGraphClient graphClient;

    @Transactional
    public String beginAuthorization(Long userId) {
        log.info("Received Outlook authorization request for Moderator user {}", userId);
        OutlookGraphConfigurationValidator.validate(properties);
        try {
            userService.getUserById(userId);
        } catch (UserException ex) {
            throw new IllegalArgumentException("Moderator user was not found.");
        }
        OutlookGraphConnectionEntity connection = connectionRepository.findById(userId)
                .orElseGet(() -> { var value = new OutlookGraphConnectionEntity(); value.setUserId(userId); return value; });
        String state = randomUrlSafe(32);
        String verifier = randomUrlSafe(48);
        connection.setPendingStateHash(sha256(state));
        connection.setPendingCodeVerifier(encrypt(verifier, properties.getTokenEncryptionKey()));
        connection.setPendingExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        connectionRepository.save(connection);
        log.info("Started Outlook Graph authorization for Moderator user {}", userId);
        String challenge = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(sha256Bytes(verifier));
        return OutlookGraphUrlBuilder.authorizationUrl(properties, state, challenge);
    }

    public String buildAdminConsentUrl() {
        log.info("Received Outlook organization-consent URL request");
        OutlookGraphConfigurationValidator.validate(properties);
        String state = randomUrlSafe(24);
        return OutlookGraphUrlBuilder.adminConsentUrl(properties, state);
    }

    public String completeAdminConsent(String consent, String oauthError) {
        log.info("Received Outlook organization-consent callback: completed={}",
                oauthError == null && "True".equalsIgnoreCase(consent));
        if (oauthError != null || !"True".equalsIgnoreCase(consent)) {
            return OutlookOAuthResultPageBuilder.build(false, "Organization approval was not completed. Ask an administrator to approve the requested calendar permissions.");
        }
        return OutlookOAuthResultPageBuilder.build(true, "Organization approval is complete. Close this window, then connect Outlook calendar sync from the add-in.");
    }

    @Transactional
    public String completeAuthorization(String code, String state, String oauthError) {
        log.info("Received Outlook authorization callback: codePresent={}, statePresent={}, providerError={}",
                code != null, state != null, oauthError != null);
        if (oauthError != null) return OutlookOAuthResultPageBuilder.build(false, "Microsoft sign-in was not completed.");
        if (code == null || state == null) return OutlookOAuthResultPageBuilder.build(false, "The Microsoft sign-in response was incomplete.");
        OutlookGraphConnectionEntity connection = connectionRepository.findByPendingStateHash(sha256(state)).orElse(null);
        if (connection == null || connection.getPendingExpiresAt() == null || connection.getPendingExpiresAt().isBefore(Instant.now())) {
            return OutlookOAuthResultPageBuilder.build(false, "This sign-in link expired. Start the connection again from the add-in.");
        }
        try {
            TokenResponse tokens = graphClient.exchangeAuthorizationCode(code, decrypt(connection.getPendingCodeVerifier(), properties.getTokenEncryptionKey()));
            connection.setAccessTokenEncrypted(encrypt(requiredValue(tokens.accessToken(), "access_token"), properties.getTokenEncryptionKey()));
            connection.setRefreshTokenEncrypted(encrypt(requiredValue(tokens.refreshToken(), "refresh_token"), properties.getTokenEncryptionKey()));
            connection.setAccessTokenExpiresAt(Instant.now().plusSeconds(expiresInSeconds(tokens.expiresIn())));
            connection.setTenantId(properties.getTenant());
            connection.setPendingStateHash(null);
            connection.setPendingCodeVerifier(null);
            connection.setPendingExpiresAt(null);
            UserProfile me = graphClient.getMailboxProfile(connection);
            connection.setMicrosoftUserId(me == null ? null : me.id());
            connection.setMailboxEmail(me == null ? null : firstNonBlank(me.mail(), me.userPrincipalName()));
            connection.setConnectionStatus(STATUS_CONNECTED);
            connection.setLastSyncError(null);
            connectionRepository.save(connection);
            createSubscription(connection);
            connectionRepository.save(connection);
            log.info("Completed Outlook authorization and subscription setup for Moderator user {}", connection.getUserId());
            return OutlookOAuthResultPageBuilder.build(true, "Microsoft calendar sync is connected. You can close this window.");
        } catch (Exception ex) {
            connection.setConnectionStatus(connection.getAccessTokenEncrypted() == null ? "DISCONNECTED" : "SUBSCRIPTION_ERROR");
            connection.setLastSyncError(safeMessage(ex));
            connectionRepository.save(connection);
            log.warn("Outlook Graph authorization failed for Moderator user {}: {}", connection.getUserId(), safeMessage(ex));
            return OutlookOAuthResultPageBuilder.build(false, "Could not finish Microsoft calendar sync. Reopen the add-in and try again.");
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStatus(Long userId) {
        log.debug("Received Outlook connection status request for Moderator user {}", userId);
        OutlookGraphConnectionEntity connection = connectionRepository.findById(userId).orElse(null);
        Map<String, Object> result = new HashMap<>();
        result.put("connected", connection != null && STATUS_CONNECTED.equals(connection.getConnectionStatus())
                && connection.getSubscriptionId() != null);
        result.put("status", connection == null ? "DISCONNECTED" : connection.getConnectionStatus());
        result.put("mailboxEmail", connection == null ? null : connection.getMailboxEmail());
        result.put("subscriptionExpiresAt", connection == null ? null : connection.getSubscriptionExpiresAt());
        result.put("message", connection == null ? null : connection.getLastSyncError());
        log.debug("Returning Outlook connection status for user {}: status={}, connected={}",
                userId, result.get("status"), result.get("connected"));
        return result;
    }

    @Transactional
    public void handleNotifications(NotificationPayload payload) {
        int notificationCount = payload == null || payload.value() == null ? 0 : payload.value().size();
        log.info("Received Outlook Graph notification batch: count={}", notificationCount);
        if (payload == null || payload.value() == null) return;
        for (Notification notification : payload.value()) {
            String subscriptionId = notification.subscriptionId();
            OutlookGraphConnectionEntity connection = connectionRepository.findBySubscriptionId(subscriptionId).orElse(null);
            if (connection == null || !constantTimeEquals(connection.getSubscriptionClientState(), notification.clientState())) {
                log.warn("Ignored Outlook Graph notification with unknown subscription or invalid client state");
                continue;
            }
            String lifecycle = notification.lifecycleEvent();
            if ("reauthorizationRequired".equals(lifecycle)) {
                log.info("Outlook Graph requires reauthorization for Moderator user {}", connection.getUserId());
                connection.setConnectionStatus("REAUTH_REQUIRED");
                connectionRepository.save(connection);
                continue;
            }
            if ("missed".equals(lifecycle)) {
                log.info("Outlook Graph reported missed notifications for Moderator user {}", connection.getUserId());
                connection.setReconciliationRequired(true);
                connectionRepository.save(connection);
                continue;
            }
            if ("subscriptionRemoved".equals(lifecycle)) {
                log.info("Outlook Graph subscription was removed for Moderator user {}; recreating it", connection.getUserId());
                connection.setSubscriptionId(null);
                connection.setSubscriptionExpiresAt(null);
                try { createSubscription(connection); } catch (Exception ex) { connection.setLastSyncError(safeMessage(ex)); }
                connectionRepository.save(connection);
                continue;
            }
            String changeType = notification.changeType();
            String eventId = notification.resourceData() == null ? null : notification.resourceData().id();
            if (eventId == null) eventId = eventIdFromResource(notification.resource());
            if (eventId == null) {
                log.warn("Ignored Outlook Graph event notification without an event ID for user {}", connection.getUserId());
                continue;
            }
            log.info("Processing Outlook Graph event notification: userId={}, changeType={}, eventId={}",
                    connection.getUserId(), changeType, eventId);
            if ("deleted".equalsIgnoreCase(changeType)) {
                cancelConferenceByEventId(eventId);
            } else if ("updated".equalsIgnoreCase(changeType)) {
                try {
                    Event event = graphClient.getEvent(connection, eventId);
                    if (event != null && Boolean.TRUE.equals(event.isCancelled())) cancelConferenceByEventId(eventId);
                } catch (Exception ex) {
                    if (ex instanceof OutlookGraphRequestException graphException && graphException.getStatusCode() == 404) {
                        cancelConferenceByEventId(eventId);
                    } else {
                        log.warn("Could not verify Outlook event for Moderator user {}; reconciliation scheduled: {}",
                                connection.getUserId(), safeMessage(ex));
                        connection.setLastSyncError(safeMessage(ex));
                        connection.setReconciliationRequired(true);
                    }
                }
            }
        }
    }

    private void cancelConferenceByEventId(String eventId) {
        conferenceRepository.findByEventId(eventId).ifPresent(conference -> {
            conference.setStatus(ConferenceStatus.CANCELLED);
            conferenceRepository.save(conference);
            log.info("Cancelled Outlook conference after Graph event change for Moderator user {}", conference.getUserId());
        });
    }

    private void createSubscription(OutlookGraphConnectionEntity connection) throws Exception {
        String clientState = randomUrlSafe(32);
        Instant expiration = Instant.now().plus(2, ChronoUnit.DAYS);
        Map<String, Object> body = Map.of(
                "changeType", "created,updated,deleted",
                "notificationUrl", OutlookGraphUrlBuilder.notificationUrl(properties),
                "lifecycleNotificationUrl", OutlookGraphUrlBuilder.notificationUrl(properties),
                "resource", "/me/events",
                "expirationDateTime", expiration.toString(),
                "clientState", clientState);
        Subscription result = graphClient.createSubscription(connection, body);
        connection.setSubscriptionId(requiredValue(result == null ? null : result.id(), "id"));
        connection.setSubscriptionExpiresAt(Instant.parse(requiredValue(result == null ? null : result.expirationDateTime(), "expirationDateTime")));
        connection.setSubscriptionClientState(clientState);
        log.info("Created Outlook Graph subscription for Moderator user {} expiring at {}",
                connection.getUserId(), connection.getSubscriptionExpiresAt());
    }

}
