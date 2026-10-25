package com.teenthofabud.videoconference.mdr.conference.outlook.util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Shared low-level helpers for Outlook Graph request and token handling.
 */
public final class OutlookGraphUtils {
    private static final SecureRandom RANDOM = new SecureRandom();

    private OutlookGraphUtils() {
    }

    /**
     * Returns the first non-blank string, or {@code null} if none are available.
     *
     * @param values candidate values in preference order
     * @return first non-blank candidate, or {@code null}
     */
    public static String firstNonBlank(String... values) {
        for (String value : values) {
            if (!blank(value)) return value;
        }
        return null;
    }

    /**
     * Requires a non-blank value from a typed external response.
     *
     * @param value response value
     * @param fieldName external field name used in the diagnostic
     * @return validated value
     * @throws IllegalStateException when the value is missing or blank
     */
    public static String requiredValue(String value, String fieldName) {
        if (blank(value)) throw new IllegalStateException("Microsoft response did not include " + fieldName);
        return value;
    }

    /**
     * Applies the OAuth protocol's default access-token lifetime when the response omits it.
     *
     * @param expiresInSeconds lifetime returned by Microsoft, possibly absent
     * @return provided lifetime or 3600 seconds
     */
    public static long expiresInSeconds(Long expiresInSeconds) {
        return expiresInSeconds == null ? 3600L : expiresInSeconds;
    }

    /**
     * Extracts the final path segment from a Graph resource path as an event ID.
     *
     * @param resource Graph resource path
     * @return final path segment, or {@code null} for a blank or malformed path
     */
    public static String eventIdFromResource(String resource) {
        if (blank(resource)) return null;
        int index = resource.lastIndexOf('/');
        return index >= 0 && index + 1 < resource.length() ? resource.substring(index + 1) : null;
    }

    /**
     * Creates a cryptographically random URL-safe Base64 value.
     *
     * @param bytes number of random bytes to generate
     * @return URL-safe value without Base64 padding
     */
    public static String randomUrlSafe(int bytes) {
        byte[] value = new byte[bytes];
        RANDOM.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    /**
     * Calculates a URL-safe Base64 SHA-256 digest for a string.
     *
     * @param value input value
     * @return digest without Base64 padding
     */
    public static String sha256(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(sha256Bytes(value));
    }

    /**
     * Calculates the SHA-256 digest bytes for a string.
     *
     * @param value input value
     * @return SHA-256 digest
     */
    public static byte[] sha256Bytes(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    /**
     * Compares two strings without returning early based on the first differing byte.
     *
     * @param expected trusted value
     * @param actual supplied value
     * @return {@code true} only when both values are present and equal
     */
    public static boolean constantTimeEquals(String expected, String actual) {
        return expected != null && actual != null
                && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encodes a string for use as a URL query or form parameter.
     *
     * @param value value to encode
     * @return UTF-8 URL-encoded value
     */
    public static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Tests whether a string is absent or contains only whitespace.
     *
     * @param value value to check
     * @return {@code true} for {@code null}, empty, or whitespace-only values
     */
    public static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Returns a bounded exception message suitable for storing as a sync diagnostic.
     *
     * @param ex exception to summarize
     * @return exception message, limited to 500 characters
     */
    public static String safeMessage(Exception ex) {
        String message = ex.getMessage();
        return message == null ? ex.getClass().getSimpleName() : message.substring(0, Math.min(message.length(), 500));
    }

    /**
     * Encrypts token data using AES-256-GCM and a fresh 12-byte IV.
     * The returned Base64 value contains the IV followed by the authenticated ciphertext.
     *
     * @param plaintext token or verifier to encrypt
     * @param base64Key configured AES key encoded as Base64
     * @return encrypted value encoded as Base64
     */
    public static String encrypt(String plaintext, String base64Key) {
        try {
            byte[] iv = new byte[12];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey(base64Key), new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] output = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, output, 0, iv.length);
            System.arraycopy(encrypted, 0, output, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(output);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not encrypt Outlook token data", ex);
        }
    }

    /**
     * Decrypts an AES-256-GCM token value produced by {@link #encrypt(String, String)}.
     *
     * @param encrypted Base64 value containing the IV and ciphertext
     * @param base64Key configured AES key encoded as Base64
     * @return decrypted token or verifier
     */
    public static String decrypt(String encrypted, String base64Key) {
        try {
            byte[] input = Base64.getDecoder().decode(encrypted);
            byte[] iv = java.util.Arrays.copyOfRange(input, 0, 12);
            byte[] cipherText = java.util.Arrays.copyOfRange(input, 12, input.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey(base64Key), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not decrypt Outlook token data; verify the configured encryption key", ex);
        }
    }

    /**
     * Validates that the configured encryption key is present, valid Base64, and exactly 256 bits.
     *
     * @param configured AES key encoded as Base64
     * @throws IllegalStateException when the key is missing, malformed, or the wrong size
     */
    public static void validateEncryptionKey(String configured) {
        encryptionKey(configured);
    }

    private static SecretKeySpec encryptionKey(String configured) {
        if (blank(configured)) throw new IllegalStateException("OUTLOOK_GRAPH_TOKEN_ENCRYPTION_KEY is required");
        byte[] key = Base64.getDecoder().decode(configured);
        if (key.length != 32) throw new IllegalStateException("OUTLOOK_GRAPH_TOKEN_ENCRYPTION_KEY must decode to exactly 32 bytes");
        return new SecretKeySpec(key, "AES");
    }
}
