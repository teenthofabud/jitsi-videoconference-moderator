package com.teenthofabud.videoconference.mdr.conference.form.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
    GoogleCalendarEvent calendarEvent;

    @JsonProperty("authorizationEventObject")
    @Schema(description = "Authentication details")
    GoogleAuthObject authEventObject;
}
