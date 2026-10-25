package com.teenthofabud.videoconference.mdr.conference.outlook.client;

import tools.jackson.databind.ObjectMapper;
import com.teenthofabud.videoconference.mdr.configuration.outlook.OutlookGraphProperties;
import com.teenthofabud.videoconference.mdr.conference.outlook.dto.OutlookGraphDtos.Event;
import com.teenthofabud.videoconference.mdr.conference.outlook.dto.OutlookGraphDtos.Subscription;
import com.teenthofabud.videoconference.mdr.conference.outlook.dto.OutlookGraphDtos.TokenResponse;
import com.teenthofabud.videoconference.mdr.conference.outlook.dto.OutlookGraphDtos.UserProfile;
import com.teenthofabud.videoconference.mdr.conference.outlook.entity.OutlookGraphConnectionEntity;
import com.teenthofabud.videoconference.mdr.conference.outlook.repository.OutlookGraphConnectionRepository;
import com.teenthofabud.videoconference.mdr.conference.outlook.util.OutlookGraphUrlBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.teenthofabud.videoconference.mdr.conference.outlook.util.OutlookGraphUtils.*;

/** Handles authenticated HTTP requests to Microsoft identity and Graph endpoints. */
@Component
@RequiredArgsConstructor
public class OutlookGraphClient {
    private final OutlookGraphProperties properties;
    private final OutlookGraphConnectionRepository connectionRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(10)).build();

    /** Exchanges the authorization code for the signed-in user's OAuth tokens. */
    public TokenResponse exchangeAuthorizationCode(String code, String verifier) throws Exception {
        Map<String, String> form = new HashMap<>();
        form.put("client_id", properties.getClientId());
        form.put("client_secret", properties.getClientSecret());
        form.put("grant_type", "authorization_code");
        form.put("code", code);
        form.put("redirect_uri", properties.getRedirectUri());
        form.put("code_verifier", verifier);
        form.put("scope", properties.getScopes());
        return postForm(OutlookGraphUrlBuilder.tokenEndpoint(properties), form, TokenResponse.class);
    }

    /** Reads the profile associated with the user's cached delegated token. */
    public UserProfile getMailboxProfile(OutlookGraphConnectionEntity connection) throws Exception {
        return graphRequest(connection, "GET", OutlookGraphUrlBuilder.GRAPH_BASE_URL
                + "/me?$select=id,displayName,mail,userPrincipalName", null, UserProfile.class);
    }

    /** Creates a Graph event subscription using the supplied subscription request fields. */
    public Subscription createSubscription(OutlookGraphConnectionEntity connection, Map<String, Object> body) throws Exception {
        return graphRequest(connection, "POST", OutlookGraphUrlBuilder.GRAPH_BASE_URL + "/subscriptions", body, Subscription.class);
    }

    /** Reads an event's cancellation state from the connected user's mailbox. */
    public Event getEvent(OutlookGraphConnectionEntity connection, String eventId) throws Exception {
        return graphRequest(connection, "GET", OutlookGraphUrlBuilder.eventUrl(eventId), null, Event.class);
    }

    private String getAccessToken(OutlookGraphConnectionEntity connection, boolean forceRefresh) throws Exception {
        if (!forceRefresh && connection.getAccessTokenExpiresAt() != null
                && connection.getAccessTokenExpiresAt().isAfter(Instant.now().plusSeconds(120))) {
            return decrypt(connection.getAccessTokenEncrypted(), properties.getTokenEncryptionKey());
        }
        String refreshToken = decrypt(connection.getRefreshTokenEncrypted(), properties.getTokenEncryptionKey());
        TokenResponse tokens = postForm(OutlookGraphUrlBuilder.tokenEndpoint(properties), Map.of(
                "client_id", properties.getClientId(),
                "client_secret", properties.getClientSecret(),
                "grant_type", "refresh_token",
                "refresh_token", refreshToken,
                "redirect_uri", properties.getRedirectUri(),
                "scope", properties.getScopes()), TokenResponse.class);
        connection.setAccessTokenEncrypted(encrypt(requiredValue(tokens.accessToken(), "access_token"), properties.getTokenEncryptionKey()));
        if (!blank(tokens.refreshToken())) {
            connection.setRefreshTokenEncrypted(encrypt(tokens.refreshToken(), properties.getTokenEncryptionKey()));
        }
        connection.setAccessTokenExpiresAt(Instant.now().plusSeconds(expiresInSeconds(tokens.expiresIn())));
        connectionRepository.save(connection);
        return decrypt(connection.getAccessTokenEncrypted(), properties.getTokenEncryptionKey());
    }

    private <T> T graphRequest(OutlookGraphConnectionEntity connection, String method, String url,
                               Map<String, Object> body, Class<T> responseType) throws Exception {
        String token = getAccessToken(connection, false);
        HttpResponse<String> response = send(method, url, token, body);
        if (response.statusCode() == 401) {
            token = getAccessToken(connection, true);
            response = send(method, url, token, body);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new OutlookGraphRequestException(response.statusCode(),
                    "Microsoft Graph request failed (" + response.statusCode() + "): " + response.body());
        }
        if (blank(response.body())) return null;
        return objectMapper.readValue(response.body(), responseType);
    }

    private HttpResponse<String> send(String method, String url, String token, Map<String, Object> body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).timeout(java.time.Duration.ofSeconds(12))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json");
        if (body == null) builder.method(method, HttpRequest.BodyPublishers.noBody());
        else builder.method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private <T> T postForm(String url, Map<String, String> fields, Class<T> responseType) throws Exception {
        String body = fields.entrySet().stream()
                .map(entry -> enc(entry.getKey()) + "=" + enc(entry.getValue()))
                .collect(java.util.stream.Collectors.joining("&"));
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(java.time.Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Microsoft OAuth request failed (" + response.statusCode() + "): " + response.body());
        }
        return objectMapper.readValue(response.body(), responseType);
    }
}
