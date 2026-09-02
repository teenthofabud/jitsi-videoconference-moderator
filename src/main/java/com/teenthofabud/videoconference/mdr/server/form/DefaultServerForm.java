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
public class DefaultServerForm {


    @Schema(description = "Name of the server",required = true)
    @NotNull(message = "server name can't be null")
    String name;

    @Schema(description = "URL of the server which user prefers for conducting conferences",required = true)
    @NotNull(message = "server URL can't be null")
    String serverUrl;

}
