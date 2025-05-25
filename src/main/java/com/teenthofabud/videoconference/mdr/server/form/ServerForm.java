package com.teenthofabud.videoconference.mdr.server.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ServerForm{

    @Schema(description = "Server name")
    String name;

    @Schema(description = "Base Url for the server")
    String serverUrl;

}
