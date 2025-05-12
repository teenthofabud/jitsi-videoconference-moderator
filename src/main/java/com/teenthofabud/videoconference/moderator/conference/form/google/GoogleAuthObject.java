package com.teenthofabud.videoconference.moderator.conference.form.google;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GoogleAuthObject {

    @JsonProperty("userOAuthToken")
    String userOAuthToken;

    @JsonProperty("userIdToken")
    String userIdToken;

    @JsonProperty("systemIdToken")
    String systemIdToken;
}
