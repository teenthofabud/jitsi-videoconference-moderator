package com.teenthofabud.videoconference.moderator.conference.form.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class GoogleConferenceForm {

    @JsonProperty("calendar")
    GoogleCalendarEvent calendarEvent;

    @JsonProperty("authorizationEventObject")
    GoogleAuthObject authEventObject;
}
