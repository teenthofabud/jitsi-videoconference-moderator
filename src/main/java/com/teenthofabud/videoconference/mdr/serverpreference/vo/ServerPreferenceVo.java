package com.teenthofabud.videoconference.mdr.serverpreference.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ServerPreferenceVo {

    @Schema(description = "Identifier for the serverPreference (PK)")
    Long id;

    @Schema(description = "Base URL of the server the user prefers")
    String serverUrl;

    @Schema(description = "Identifier for the user")
    String userId;

    @Schema(description = "Server preference type whether DEFAULT or CUSTOM")
    String serverPreferenceType;


}
