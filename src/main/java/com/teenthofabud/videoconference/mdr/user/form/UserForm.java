package com.teenthofabud.videoconference.mdr.user.form;

import com.teenthofabud.videoconference.mdr.user.constants.ReferenceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserForm {

    @Schema(description = "Email of the user",required = true)
    @NotNull(message = "email can't be null")
    String email;

    @Schema(description = "Name of the user")
    String name;

}
