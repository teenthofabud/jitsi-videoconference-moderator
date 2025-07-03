package com.teenthofabud.videoconference.mdr.conference.vo;

import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ConferenceVo {


    @Schema(description = "Room identifier for the conference")
    String roomId;

    @Schema(description = "Identifier for the user who created the conference")
    String userId;

    @Schema(description = "Base URL of the server")
    String serverUrl;

    @Schema(description = "Identifier for the server preference which has the userId and serverId details")
    Long serverPreferenceId;

    @Schema(description = "Whether the conference is still ACTIVE (upcoming/ongoing) or INACTIVE(canceled/deleted)")
    ConferenceStatus status;


}
