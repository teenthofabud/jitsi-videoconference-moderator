package com.teenthofabud.videoconference.mdr.user.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ServerPreferenceForm {

    @Schema(description = "Email of the user",required = true)
    @NotNull(message = "email can't be null")
    String email;

    @Schema(description = "Name of the server",required = true)
    @NotNull(message = "server name can't be null")
    String name;

    @Schema(description = "URL of the server which user prefers for conducting conferences",required = true)
    @NotNull(message = "preferred server URL can't be null")
    String preferredServerUrl;

}
