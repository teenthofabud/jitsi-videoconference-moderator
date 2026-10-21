package com.teenthofabud.videoconference.mdr.conference.outlook.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teenthofabud.videoconference.mdr.configuration.outlook.OutlookGraphProperties;
import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import com.teenthofabud.videoconference.mdr.conference.outlook.entity.OutlookGraphTokenCacheEntity;
import com.teenthofabud.videoconference.mdr.conference.outlook.repository.OutlookConferenceRepository;
import com.teenthofabud.videoconference.mdr.conference.outlook.repository.OutlookGraphTokenCacheRepository;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OutlookGraphSyncService {
    private static final String GRAPH = "https://graph.microsoft.com/v1.0";
    private static final String STATUS_CONNECTED = "CONNECTED";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OutlookGraphProperties properties;
    private final OutlookGraphTokenCacheRepository tokenRepository;
    private final OutlookConferenceRepository conferenceRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(10)).build();

    @Transactional
    public String beginAuthorization(Long userId) {
        requireConfiguration();
        try {
            userService.getUserById(userId);
        } catch (UserException ex) {
            throw new IllegalArgumentException("Moderator user was not found.");
        }
        OutlookGraphTokenCacheEntity cache = tokenRepository.findById(userId)
                .orElseGet(() -> { var value = new OutlookGraphTokenCacheEntity(); value.setUserId(userId); return value; });
        String state = randomUrlSafe(32);
        String verifier = randomUrlSafe(48);
        cache.setPendingStateHash(sha256(state));
        cache.setPendingCodeVerifier(encrypt(verifier));
        cache.setPendingExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        tokenRepository.save(cache);
        String challenge = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(sha256Bytes(verifier));
        String authorize = "https://login.microsoftonline.com/" + enc(properties.getTenant())
                + "/oauth2/v2.0/authorize?client_id=" + enc(properties.getClientId())
                + "&response_type=code&redirect_uri=" + enc(properties.getRedirectUri())
                + "&response_mode=query&scope=" + enc(properties.getScopes())
                + "&state=" + enc(state) + "&code_challenge=" + enc(challenge)
                + "&code_challenge_method=S256&prompt=select_account";
        return authorize;
    }

    public String buildAdminConsentUrl() {
        requireConfiguration();
        String tenant = "common".equalsIgnoreCase(properties.getTenant()) ? "organizations" : properties.getTenant();
        String delegatedScopes = java.util.Arrays.stream(properties.getScopes().split("\\s+"))
                .filter(scope -> !scope.equalsIgnoreCase("openid") && !scope.equalsIgnoreCase("profile")
                        && !scope.equalsIgnoreCase("offline_access"))
                .map(scope -> scope.startsWith("https://") ? scope : "https://graph.microsoft.com/" + scope)
                .collect(java.util.stream.Collectors.joining(" "));
        String state = randomUrlSafe(24);
        return "https://login.microsoftonline.com/" + enc(tenant) + "/v2.0/adminconsent?client_id="
                + enc(properties.getClientId()) + "&scope=" + enc(delegatedScopes)
                + "&redirect_uri=" + enc(properties.getRedirectUri()) + "&state=" + enc(state);
    }

    public String completeAdminConsent(String consent, String oauthError) {
        if (oauthError != null || !"True".equalsIgnoreCase(consent)) {
            return resultPage(false, "Organization approval was not completed. Ask an administrator to approve the requested calendar permissions.");
        }
        return resultPage(true, "Organization approval is complete. Close this window, then connect Outlook calendar sync from the add-in.");
    }

    @Transactional
    public String completeAuthorization(String code, String state, String oauthError) {
        if (oauthError != null) return resultPage(false, "Microsoft sign-in was not completed.");
        if (code == null || state == null) return resultPage(false, "The Microsoft sign-in response was incomplete.");
        OutlookGraphTokenCacheEntity cache = tokenRepository.findByPendingStateHash(sha256(state)).orElse(null);
        if (cache == null || cache.getPendingExpiresAt() == null || cache.getPendingExpiresAt().isBefore(Instant.now())) {
            return resultPage(false, "This sign-in link expired. Start the connection again from the add-in.");
        }
        try {
            JsonNode tokens = exchangeCode(code, decrypt(cache.getPendingCodeVerifier()));
            cache.setAccessTokenEncrypted(encrypt(requiredText(tokens, "access_token")));
            cache.setRefreshTokenEncrypted(encrypt(requiredText(tokens, "refresh_token")));
            cache.setAccessTokenExpiresAt(Instant.now().plusSeconds(tokens.path("expires_in").asLong(3600)));
            cache.setTenantId(properties.getTenant());
            cache.setPendingStateHash(null);
            cache.setPendingCodeVerifier(null);
            cache.setPendingExpiresAt(null);
            JsonNode me = graphRequest(cache, "GET", GRAPH + "/me?$select=id,displayName,mail,userPrincipalName", null);
            cache.setMicrosoftUserId(text(me, "id"));
            cache.setMailboxEmail(firstText(me, "mail", "userPrincipalName"));
            cache.setConnectionStatus(STATUS_CONNECTED);
            cache.setLastSyncError(null);
            tokenRepository.save(cache);
            createSubscription(cache);
            tokenRepository.save(cache);
            return resultPage(true, "Microsoft calendar sync is connected. You can close this window.");
        } catch (Exception ex) {
            cache.setConnectionStatus(cache.getAccessTokenEncrypted() == null ? "DISCONNECTED" : "SUBSCRIPTION_ERROR");
            cache.setLastSyncError(safeMessage(ex));
            tokenRepository.save(cache);
            return resultPage(false, "Could not finish Microsoft calendar sync. Reopen the add-in and try again.");
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStatus(Long userId) {
        OutlookGraphTokenCacheEntity cache = tokenRepository.findById(userId).orElse(null);
        Map<String, Object> result = new HashMap<>();
        result.put("connected", cache != null && STATUS_CONNECTED.equals(cache.getConnectionStatus())
                && cache.getSubscriptionId() != null);
        result.put("status", cache == null ? "DISCONNECTED" : cache.getConnectionStatus());
        result.put("mailboxEmail", cache == null ? null : cache.getMailboxEmail());
        result.put("subscriptionExpiresAt", cache == null ? null : cache.getSubscriptionExpiresAt());
        result.put("message", cache == null ? null : cache.getLastSyncError());
        return result;
    }

    @Transactional
    public void handleNotifications(JsonNode payload) {
        JsonNode notifications = payload.path("value");
        if (!notifications.isArray()) return;
        for (JsonNode notification : notifications) {
            String subscriptionId = text(notification, "subscriptionId");
            OutlookGraphTokenCacheEntity cache = tokenRepository.findBySubscriptionId(subscriptionId).orElse(null);
            if (cache == null || !constantTimeEquals(cache.getSubscriptionClientState(), text(notification, "clientState"))) continue;
            String lifecycle = text(notification, "lifecycleEvent");
            if ("reauthorizationRequired".equals(lifecycle)) {
                cache.setConnectionStatus("REAUTH_REQUIRED");
                tokenRepository.save(cache);
                continue;
            }
            if ("missed".equals(lifecycle)) {
                cache.setReconciliationRequired(true);
                tokenRepository.save(cache);
                continue;
            }
            if ("subscriptionRemoved".equals(lifecycle)) {
                cache.setSubscriptionId(null);
                cache.setSubscriptionExpiresAt(null);
                try { createSubscription(cache); } catch (Exception ex) { cache.setLastSyncError(safeMessage(ex)); }
                tokenRepository.save(cache);
                continue;
            }
            String changeType = text(notification, "changeType");
            String eventId = firstText(notification.path("resourceData"), "id", null);
            if (eventId == null) eventId = eventIdFromResource(text(notification, "resource"));
            if (eventId == null) continue;
            if ("deleted".equalsIgnoreCase(changeType)) {
                cancelConferenceByEventId(eventId);
            } else if ("updated".equalsIgnoreCase(changeType)) {
                try {
                    JsonNode event = graphRequest(cache, "GET", GRAPH + "/me/events/" + enc(eventId) + "?$select=id,isCancelled", null);
                    if (event.path("isCancelled").asBoolean(false)) cancelConferenceByEventId(eventId);
                } catch (Exception ex) {
                    if (ex instanceof GraphException graphException && graphException.statusCode == 404) {
                        cancelConferenceByEventId(eventId);
                    } else {
                        cache.setLastSyncError(safeMessage(ex));
                        cache.setReconciliationRequired(true);
                    }
                }
            }
        }
    }

    private void cancelConferenceByEventId(String eventId) {
        conferenceRepository.findByEventId(eventId).ifPresent(conference -> {
            conference.setStatus(ConferenceStatus.CANCELLED);
            conferenceRepository.save(conference);
        });
    }

    private void createSubscription(OutlookGraphTokenCacheEntity cache) throws Exception {
        String clientState = randomUrlSafe(32);
        Instant expiration = Instant.now().plus(2, ChronoUnit.DAYS);
        Map<String, Object> body = Map.of(
                "changeType", "created,updated,deleted",
                "notificationUrl", properties.getPublicBaseUrl().replaceAll("/+$", "") + "/api/outlook/graph/notifications",
                "lifecycleNotificationUrl", properties.getPublicBaseUrl().replaceAll("/+$", "") + "/api/outlook/graph/notifications",
                "resource", "/me/events",
                "expirationDateTime", expiration.toString(),
                "clientState", clientState);
        JsonNode result = graphRequest(cache, "POST", GRAPH + "/subscriptions", body);
        cache.setSubscriptionId(requiredText(result, "id"));
        cache.setSubscriptionExpiresAt(Instant.parse(requiredText(result, "expirationDateTime")));
        cache.setSubscriptionClientState(clientState);
    }

    private JsonNode exchangeCode(String code, String verifier) throws Exception {
        Map<String, String> form = new HashMap<>();
        form.put("client_id", properties.getClientId());
        form.put("client_secret", properties.getClientSecret());
        form.put("grant_type", "authorization_code");
        form.put("code", code);
        form.put("redirect_uri", properties.getRedirectUri());
        form.put("code_verifier", verifier);
        form.put("scope", properties.getScopes());
        return postForm(tokenEndpoint(), form);
    }

    private String getAccessToken(OutlookGraphTokenCacheEntity cache, boolean forceRefresh) throws Exception {
        if (!forceRefresh && cache.getAccessTokenExpiresAt() != null
                && cache.getAccessTokenExpiresAt().isAfter(Instant.now().plusSeconds(120))) {
            return decrypt(cache.getAccessTokenEncrypted());
        }
        String refresh = decrypt(cache.getRefreshTokenEncrypted());
        JsonNode tokens = postForm(tokenEndpoint(), Map.of(
                "client_id", properties.getClientId(),
                "client_secret", properties.getClientSecret(),
                "grant_type", "refresh_token",
                "refresh_token", refresh,
                "redirect_uri", properties.getRedirectUri(),
                "scope", properties.getScopes()));
        cache.setAccessTokenEncrypted(encrypt(requiredText(tokens, "access_token")));
        if (tokens.hasNonNull("refresh_token")) cache.setRefreshTokenEncrypted(encrypt(tokens.get("refresh_token").asText()));
        cache.setAccessTokenExpiresAt(Instant.now().plusSeconds(tokens.path("expires_in").asLong(3600)));
        tokenRepository.save(cache);
        return decrypt(cache.getAccessTokenEncrypted());
    }

    private JsonNode graphRequest(OutlookGraphTokenCacheEntity cache, String method, String url,
                                  Map<String, Object> body) throws Exception {
        String token = getAccessToken(cache, false);
        HttpResponse<String> response = send(method, url, token, body);
        if (response.statusCode() == 401) {
            token = getAccessToken(cache, true);
            response = send(method, url, token, body);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new GraphException(response.statusCode(), "Microsoft Graph request failed (" + response.statusCode() + "): " + response.body());
        }
        if (response.body() == null || response.body().isBlank()) return objectMapper.createObjectNode();
        return objectMapper.readTree(response.body());
    }

    private HttpResponse<String> send(String method, String url, String token, Map<String, Object> body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).timeout(java.time.Duration.ofSeconds(12))
                .header("Authorization", "Bearer " + token).header("Accept", "application/json")
                .header("Content-Type", "application/json");
        if (body == null) builder.method(method, HttpRequest.BodyPublishers.noBody());
        else builder.method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode postForm(String url, Map<String, String> fields) throws Exception {
        String body = fields.entrySet().stream().map(entry -> enc(entry.getKey()) + "=" + enc(entry.getValue()))
                .collect(java.util.stream.Collectors.joining("&"));
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(java.time.Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Microsoft OAuth request failed (" + response.statusCode() + "): " + response.body());
        }
        return objectMapper.readTree(response.body());
    }

    private String tokenEndpoint() {
        return "https://login.microsoftonline.com/" + enc(properties.getTenant()) + "/oauth2/v2.0/token";
    }

    private String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[12]; RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey(), new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] output = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, output, 0, iv.length);
            System.arraycopy(encrypted, 0, output, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(output);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not encrypt Outlook token data", ex);
        }
    }

    private String decrypt(String encrypted) {
        try {
            byte[] input = Base64.getDecoder().decode(encrypted);
            byte[] iv = java.util.Arrays.copyOfRange(input, 0, 12);
            byte[] cipherText = java.util.Arrays.copyOfRange(input, 12, input.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey(), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not decrypt Outlook token data; verify the configured encryption key", ex);
        }
    }

    private SecretKeySpec encryptionKey() {
        String configured = properties.getTokenEncryptionKey();
        if (configured == null || configured.isBlank()) throw new IllegalStateException("OUTLOOK_GRAPH_TOKEN_ENCRYPTION_KEY is required");
        byte[] key = Base64.getDecoder().decode(configured);
        if (key.length != 32) throw new IllegalStateException("OUTLOOK_GRAPH_TOKEN_ENCRYPTION_KEY must decode to exactly 32 bytes");
        return new SecretKeySpec(key, "AES");
    }

    private void requireConfiguration() {
        if (blank(properties.getClientId()) || blank(properties.getClientSecret()) || blank(properties.getRedirectUri())
                || blank(properties.getPublicBaseUrl()) || blank(properties.getTenant())) {
            throw new IllegalStateException("Outlook Graph OAuth configuration is incomplete");
        }
        encryptionKey();
    }

    private String resultPage(boolean success, String message) {
        String color = success ? "#107c10" : "#a4262c";
        String safe = message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
        return "<!doctype html><html><head><meta charset=\"utf-8\"><title>TOAB Jitsi</title></head>"
                + "<body style=\"font:16px Arial,sans-serif;padding:24px\"><p style=\"color:" + color + "\">" + safe + "</p>"
                + "<script>if(window.opener){window.opener.postMessage({type:'toab-outlook-graph-connected'},'*');}"
                + "setTimeout(function(){window.close()},1200);</script></body></html>";
    }

    private static String eventIdFromResource(String resource) {
        if (blank(resource)) return null;
        int index = resource.lastIndexOf('/');
        return index >= 0 && index + 1 < resource.length() ? resource.substring(index + 1) : null;
    }

    private static String firstText(JsonNode node, String first, String second) {
        String value = text(node, first);
        return value == null && second != null ? text(node, second) : value;
    }

    private static String text(JsonNode node, String name) {
        JsonNode value = node == null ? null : node.get(name);
        return value == null || value.isNull() || value.asText().isBlank() ? null : value.asText();
    }

    private static String requiredText(JsonNode node, String name) {
        String value = text(node, name);
        if (value == null) throw new IllegalStateException("Microsoft response did not include " + name);
        return value;
    }

    private static String randomUrlSafe(int bytes) {
        byte[] value = new byte[bytes]; RANDOM.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private static String sha256(String value) { return Base64.getUrlEncoder().withoutPadding().encodeToString(sha256Bytes(value)); }
    private static byte[] sha256Bytes(String value) {
        try { return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)); }
        catch (Exception ex) { throw new IllegalStateException(ex); }
    }
    private static boolean constantTimeEquals(String expected, String actual) {
        return expected != null && actual != null && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }
    private static String enc(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
    private static boolean blank(String value) { return value == null || value.isBlank(); }
    private static String safeMessage(Exception ex) {
        String message = ex.getMessage();
        return message == null ? ex.getClass().getSimpleName() : message.substring(0, Math.min(message.length(), 500));
    }

    private static final class GraphException extends Exception {
        private final int statusCode;
        private GraphException(int statusCode, String message) { super(message); this.statusCode = statusCode; }
    }
}
