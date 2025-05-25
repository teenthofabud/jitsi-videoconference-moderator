package com.teenthofabud.videoconference.mdr.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ServerDto {

    @Schema(description = "Server name")
    String name;

    @Schema(description = "Base Url for the server")
    String serverUrl;

}
