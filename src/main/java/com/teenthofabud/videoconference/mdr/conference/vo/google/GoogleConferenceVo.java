package com.teenthofabud.videoconference.mdr.conference.vo.google;

import com.teenthofabud.videoconference.mdr.conference.vo.ConferenceVo;
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
public class GoogleConferenceVo extends ConferenceVo {

    @Schema(description = "Google Calendar Event ID")
    String eventId;
}
