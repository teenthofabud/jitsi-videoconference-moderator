package com.teenthofabud.videoconference.mdr.conference.form.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class GoogleCalendarEvent {

    @JsonProperty("calendarId")
    @Schema(description = "Google calendar id of the user creating the conference")
    @NotNull(message = "calendarId can't be null")
    @Email
    String calendarId;

    @JsonProperty("id")
    @Schema(description = "Event id")
    @NotNull(message = "eventId can't be null")
    String eventId;

}
