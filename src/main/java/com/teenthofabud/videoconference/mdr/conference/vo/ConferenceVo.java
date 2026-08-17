package com.teenthofabud.videoconference.mdr.conference.vo;

import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
public class ConferenceVo {

    @Schema(description = "Room identifier for the conference")
    String roomId;

    @Schema(description = "Whether the conference is still ACTIVE, PENDING, or CANCELLED")
    ConferenceStatus status;

}
