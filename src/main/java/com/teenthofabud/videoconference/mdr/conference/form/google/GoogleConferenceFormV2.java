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
public class GoogleConferenceFormV2 {

    @JsonProperty("eventId")
    @NotNull
    String eventId;

    @JsonProperty("email")
    @NotNull
    String email;

    @JsonProperty("serverUrl")
    @NotNull
    String serverUrl;

}
