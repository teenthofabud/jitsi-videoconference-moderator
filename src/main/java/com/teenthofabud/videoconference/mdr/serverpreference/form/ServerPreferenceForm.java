package com.teenthofabud.videoconference.mdr.serverpreference.form;

import com.teenthofabud.videoconference.mdr.serverpreference.constants.ServerPreferenceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode
public class ServerPreferenceForm {

    @Schema(name = "userId", description = "Identifier for the user, eg:- email", required = true)
    @NotNull(message = "userId is required")
    String userId;

    @Schema(name = "serverUrl", description = "Identifier/base URL for the server", required = true )
    @NotNull(message = "serverUrl is required")
    String serverUrl;

    @Schema(description = "Server preference type whether DEFAULT or CUSTOM", required = true, allowableValues = {"DEFAULT","CUSTOM"})
    String serverPreferenceType;
}
