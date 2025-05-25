package com.teenthofabud.videoconference.mdr.user.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserForm {

    @Schema(description = "Identifier for the user who created the conference")
    String userId;

    @Schema(description = "Name of the user")
    String name;

    @Schema(description = "User reference type, eg:- GMAIL")
    String referenceType;
}
