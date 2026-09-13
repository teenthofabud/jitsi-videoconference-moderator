package com.teenthofabud.videoconference.mdr.conference.vo.google;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class GoogleConferenceDataVo {

    @Schema(description = "Room identifier for the conference")
    String id;

    @Schema(description = "Event ID for the conference generated from Google calendar on createConference")
    String eventId;

    @Schema(description = "Identifier for the user who created the conference")
    Long userId;

    @Schema(description = "Video Entry Point")
    String videoUri;

}
