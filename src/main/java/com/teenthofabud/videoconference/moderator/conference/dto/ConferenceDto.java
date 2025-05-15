package com.teenthofabud.videoconference.moderator.conference.dto;

import com.teenthofabud.videoconference.moderator.conference.constants.ConferenceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ConferenceDto {

    @Schema(description = "Room identifier for the conference")
    String roomId;

    @Schema(description = "Identifier for the user who created the conference")
    String userRef;

    @Schema(description = "Base URL of the server")
    String serverUrl;

    @Schema(description = "Whether the conference is still ACTIVE (upcoming/ongoing) or INACTIVE(canceled/deleted)")
    ConferenceStatus status;

    @Schema(description = "Error message in case any")
    String error;

}
