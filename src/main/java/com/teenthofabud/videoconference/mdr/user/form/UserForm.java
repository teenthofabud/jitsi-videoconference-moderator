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
public class UserForm {

    @Schema(description = "Identifier for the user",required = true)
    @NotNull(message = "user id can't be null")
    String userId;

    @Schema(description = "Name of the user",required = true)
    @NotNull(message = "name can't be null")
    String name;

    @Schema(description = "User reference type, eg:- GMAIL",required = true)
    @NotNull(message = "reference type can't be null")
    String referenceType;

    @Schema(description = "Server preference type whether DEFAULT or CUSTOM", required = true, allowableValues = {"DEFAULT","CUSTOM"})
    String serverPreferenceType;

    @Schema(description = "URL of the server which user prefers for conducting the conference ",required = true)
    @NotNull(message = "preferred server URL can't be null")
    String preferredServerUrl;
}
