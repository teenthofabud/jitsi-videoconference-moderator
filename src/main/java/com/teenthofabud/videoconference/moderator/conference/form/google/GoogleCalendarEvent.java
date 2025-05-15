package com.teenthofabud.videoconference.moderator.conference.form.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.JsonObject;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class GoogleCalendarEvent {

    @JsonProperty("calendarId")
    String calendarId;

    @JsonProperty("id")
    String eventId;

    //unknown for now
    @JsonProperty("capabilities")
    JsonObject capabilities;

    //unknown for now
    @JsonProperty("organizer")
    JsonObject organizer;
}
