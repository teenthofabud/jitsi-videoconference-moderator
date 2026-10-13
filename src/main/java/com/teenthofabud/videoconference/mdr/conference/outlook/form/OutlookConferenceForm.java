package com.teenthofabud.videoconference.mdr.conference.outlook.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class OutlookConferenceForm {

    @Schema(description = "Stable add-in generated identifier persisted in appointment metadata")
    @NotBlank
    String draftTrackingId;

    @Schema(description = "Moderator user who owns this appointment")
    @NotNull
    Long userId;

    @Schema(description = "Selected conference server")
    @NotNull
    Long serverId;
}
