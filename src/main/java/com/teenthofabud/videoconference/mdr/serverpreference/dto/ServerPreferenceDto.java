package com.teenthofabud.videoconference.mdr.serverpreference.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ServerPreferenceDto {

    @Schema(description = "Base URL of the server the user prefers")
    String serverUrl;

    @Schema(description = "Identifier for the user")
    String userId;

}
