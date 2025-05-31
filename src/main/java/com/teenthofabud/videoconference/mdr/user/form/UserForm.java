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

    @Schema(description = "Identifier for the user")
    @NotNull(message = "user id can't be null")
    String userId;

    @Schema(description = "Name of the user")
    @NotNull(message = "name can't be null")
    String name;

    @Schema(description = "User reference type, eg:- GMAIL")
    @NotNull(message = "reference type can't be null")
    String referenceType;
}
