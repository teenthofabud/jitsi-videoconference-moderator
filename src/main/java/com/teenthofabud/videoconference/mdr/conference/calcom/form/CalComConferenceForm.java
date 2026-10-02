package com.teenthofabud.videoconference.mdr.conference.calcom.form;

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
public class CalComConferenceForm {

    @Schema(description = "Unique immutable Cal.com booking reference (event.uid)")
    @NotBlank
    String bookingUid;

    @Schema(description = "Optional Cal.com internal booking ID")
    Long bookingId;

    @Schema(description = "Identifier for the user who owns the booking")
    @NotNull
    Long userId;

    @Schema(description = "Identifier for the server where the conference will be held")
    @NotNull
    Long serverId;
}
