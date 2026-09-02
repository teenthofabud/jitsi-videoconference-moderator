package com.teenthofabud.videoconference.mdr.server.vo;

import com.teenthofabud.videoconference.mdr.server.constants.ServerType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ServerVo {

    @Schema(description = "Server ID (PK)")
    Long id;

    @Schema(description = "Server name")
    String name;

    @Schema(description = "Base Url for the server")
    String serverUrl;

    @Schema(description = "Whether the server is default or custom")
    ServerType serverType;

    @Schema(description = "User ID (PK) of the user who added this server entry")
    Long userId;


}
