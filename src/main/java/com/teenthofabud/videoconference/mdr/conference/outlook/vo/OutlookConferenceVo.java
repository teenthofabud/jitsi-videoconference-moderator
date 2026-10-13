package com.teenthofabud.videoconference.mdr.conference.outlook.vo;

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
public class OutlookConferenceVo extends ConferenceVo {

    @Schema(description = "Stable add-in generated identifier stored in appointment metadata")
    String draftTrackingId;

    @Schema(description = "Outlook calendar event ID, populated after sending")
    String eventId;

    Long userId;

    Long serverId;

    String videoUri;
}
