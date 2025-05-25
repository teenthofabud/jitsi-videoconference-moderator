package com.teenthofabud.videoconference.mdr.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserDto {

    @Schema(description = "Identifier for the user who created the conference")
    String userId;

    @Schema(description = "Name of the user")
    String name;

    @Schema(description = "User reference type, eg:- GMAIL")
    String referenceType;


}
