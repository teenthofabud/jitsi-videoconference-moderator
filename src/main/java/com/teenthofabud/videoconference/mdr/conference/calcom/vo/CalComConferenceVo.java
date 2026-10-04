package com.teenthofabud.videoconference.mdr.conference.calcom.vo;

import com.teenthofabud.videoconference.mdr.conference.vo.ConferenceVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
public class CalComConferenceVo extends ConferenceVo {

    @Schema(description = "Unique immutable Cal.com booking reference (event.uid)")
    String bookingUid;

    @Schema(description = "Cal.com internal booking ID")
    Long bookingId;

    @Schema(description = "Identifier for the user who owns the booking")
    Long userId;

    @Schema(description = "Identifier for the conference server")
    Long serverId;

    @Schema(description = "Normalized meeting URL for the conference")
    String videoUri;
}
