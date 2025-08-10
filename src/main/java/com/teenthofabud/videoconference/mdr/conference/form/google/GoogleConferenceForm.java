package com.teenthofabud.videoconference.mdr.conference.form.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class GoogleConferenceForm {

    @JsonProperty("calendar")
    @Schema(description = "Google calendar event")
    @NotNull
    GoogleCalendarEvent calendarEvent;

    @JsonProperty("authorizationEventObject")
    @Deprecated
    @Schema(description = "Authentication details")
    GoogleAuthObject authEventObject;

    @JsonProperty("clientPlatform")
    String clientPlatform;

    @JsonProperty("hostApp")
    String hostApp;

    @JsonProperty("eventData")
    @NotNull
    GoogleEventData eventData;

    @JsonProperty("commonEventObject")
    GoogleCommonEventObject commonEventObject;
}
