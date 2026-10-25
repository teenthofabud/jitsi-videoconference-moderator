package com.teenthofabud.videoconference.mdr.conference.outlook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Typed payloads for the Microsoft OAuth and Graph response fields used by Moderator.
 */
public final class OutlookGraphDtos {
    private OutlookGraphDtos() {
    }

    /** OAuth access and refresh token response fields consumed by the integration. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("expires_in") Long expiresIn) {
    }

    /** Microsoft profile fields used to identify the connected mailbox. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserProfile(String id, String displayName, String mail, String userPrincipalName) {
    }

    /** Fields returned when creating a Graph subscription. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Subscription(String id, String expirationDateTime) {
    }

    /** Cancellation state queried for a changed Outlook event. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Event(String id, Boolean isCancelled) {
    }

    /** Microsoft Graph change-notification envelope. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NotificationPayload(List<Notification> value) {
    }

    /** A single Graph change notification or lifecycle notification. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Notification(
            String subscriptionId,
            String clientState,
            String lifecycleEvent,
            String changeType,
            String resource,
            ResourceData resourceData) {
    }

    /** Resource fields included with a Graph notification. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResourceData(String id) {
    }
}
