package com.teenthofabud.videoconference.mdr.conference.outlook.util;

import com.teenthofabud.videoconference.mdr.configuration.outlook.OutlookGraphProperties;

import java.util.Arrays;
import java.util.stream.Collectors;

/** Builds Microsoft identity and Graph URLs used by the Outlook integration. */
public final class OutlookGraphUrlBuilder {
    public static final String GRAPH_BASE_URL = "https://graph.microsoft.com/v1.0";
    private static final String LOGIN_BASE_URL = "https://login.microsoftonline.com/";

    private OutlookGraphUrlBuilder() {
    }

    /**
     * Builds the Microsoft authorization URL for the authorization-code/PKCE flow.
     *
     * @param properties OAuth configuration
     * @param state single-use CSRF correlation state
     * @param challenge URL-safe PKCE challenge
     * @return Microsoft authorization URL
     */
    public static String authorizationUrl(OutlookGraphProperties properties, String state, String challenge) {
        return LOGIN_BASE_URL + OutlookGraphUtils.enc(properties.getTenant())
                + "/oauth2/v2.0/authorize?client_id=" + OutlookGraphUtils.enc(properties.getClientId())
                + "&response_type=code&redirect_uri=" + OutlookGraphUtils.enc(properties.getRedirectUri())
                + "&response_mode=query&scope=" + OutlookGraphUtils.enc(properties.getScopes())
                + "&state=" + OutlookGraphUtils.enc(state) + "&code_challenge=" + OutlookGraphUtils.enc(challenge)
                + "&code_challenge_method=S256&prompt=select_account";
    }

    /**
     * Builds the Microsoft organization admin-consent URL.
     *
     * @param properties OAuth configuration
     * @param state callback correlation value
     * @return Microsoft admin-consent URL
     */
    public static String adminConsentUrl(OutlookGraphProperties properties, String state) {
        String tenant = "common".equalsIgnoreCase(properties.getTenant()) ? "organizations" : properties.getTenant();
        String scopes = Arrays.stream(properties.getScopes().split("\\s+"))
                .filter(scope -> !scope.equalsIgnoreCase("openid") && !scope.equalsIgnoreCase("profile")
                        && !scope.equalsIgnoreCase("offline_access"))
                .map(scope -> scope.startsWith("https://") ? scope : "https://graph.microsoft.com/" + scope)
                .collect(Collectors.joining(" "));
        return LOGIN_BASE_URL + OutlookGraphUtils.enc(tenant) + "/v2.0/adminconsent?client_id="
                + OutlookGraphUtils.enc(properties.getClientId()) + "&scope=" + OutlookGraphUtils.enc(scopes)
                + "&redirect_uri=" + OutlookGraphUtils.enc(properties.getRedirectUri())
                + "&state=" + OutlookGraphUtils.enc(state);
    }

    /** Builds the OAuth token endpoint URL for the configured tenant. */
    public static String tokenEndpoint(OutlookGraphProperties properties) {
        return LOGIN_BASE_URL + OutlookGraphUtils.enc(properties.getTenant()) + "/oauth2/v2.0/token";
    }

    /** Builds the notification URL registered with Microsoft Graph. */
    public static String notificationUrl(OutlookGraphProperties properties) {
        return properties.getPublicBaseUrl().replaceAll("/+$", "") + "/api/outlook/graph/notifications";
    }

    /** Builds the Graph API URL used to inspect the cancellation state of an event. */
    public static String eventUrl(String eventId) {
        return GRAPH_BASE_URL + "/me/events/" + OutlookGraphUtils.enc(eventId) + "?$select=id,isCancelled";
    }
}
