package com.teenthofabud.videoconference.mdr.server.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ServerForm{

    @Schema(description = "Server name")
    @NotNull(message = "Server name can't be null")
    String name;

    @Schema(description = "Base Url for the server")
    @NotNull(message = "Server url can't be null")
    String serverUrl;

}
