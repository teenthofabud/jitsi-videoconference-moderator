package com.teenthofabud.videoconference.mdr.conference.outlook.util;

import com.teenthofabud.videoconference.mdr.configuration.outlook.OutlookGraphProperties;

/** Validates required Microsoft Graph OAuth configuration before starting a user flow. */
public final class OutlookGraphConfigurationValidator {
    private OutlookGraphConfigurationValidator() {
    }

    /**
     * Verifies the OAuth settings and encryption key required by the Outlook integration.
     *
     * @param properties configured Outlook Graph properties
     * @throws IllegalStateException if any required property is missing or invalid
     */
    public static void validate(OutlookGraphProperties properties) {
        if (OutlookGraphUtils.blank(properties.getClientId()) || OutlookGraphUtils.blank(properties.getClientSecret())
                || OutlookGraphUtils.blank(properties.getRedirectUri()) || OutlookGraphUtils.blank(properties.getPublicBaseUrl())
                || OutlookGraphUtils.blank(properties.getTenant())) {
            throw new IllegalStateException("Outlook Graph OAuth configuration is incomplete");
        }
        OutlookGraphUtils.validateEncryptionKey(properties.getTokenEncryptionKey());
    }
}
