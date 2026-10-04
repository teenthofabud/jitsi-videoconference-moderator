package com.teenthofabud.videoconference.mdr.conference.google.form;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class GoogleConferenceForm {

    @JsonProperty("id")
    String eventId;

    @JsonProperty("serverId")
    Long serverId;

    @JsonProperty("userId")
    Long userId;

}
