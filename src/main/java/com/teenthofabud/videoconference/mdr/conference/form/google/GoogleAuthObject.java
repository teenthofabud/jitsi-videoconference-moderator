package com.teenthofabud.videoconference.mdr.conference.form.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class GoogleAuthObject {

    @JsonProperty("userOAuthToken")
    @NotNull(message = "user OAuth token can't be null")
    String userOAuthToken;

    @JsonProperty("userIdToken")
    @NotNull(message = "user id token can't be null")
    String userIdToken;

    @JsonProperty("systemIdToken")
    @NotNull(message = "system id token can't be null")
    String systemIdToken;
}
