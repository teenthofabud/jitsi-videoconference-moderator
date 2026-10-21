package com.teenthofabud.videoconference.mdr.configuration.outlook;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "outlook.graph")
public class OutlookGraphProperties {
    private String clientId;
    private String clientSecret;
    private String tenant = "common";
    private String redirectUri;
    private String publicBaseUrl;
    private String tokenEncryptionKey;
    private String scopes = "openid profile offline_access User.Read Calendars.Read";

}
