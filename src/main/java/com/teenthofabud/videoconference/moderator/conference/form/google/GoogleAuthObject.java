package com.teenthofabud.videoconference.moderator.conference.form.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class GoogleAuthObject {

    @JsonProperty("userOAuthToken")
    String userOAuthToken;

    @JsonProperty("userIdToken")
    String userIdToken;

    @JsonProperty("systemIdToken")
    String systemIdToken;
}
