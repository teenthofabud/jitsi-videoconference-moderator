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

    @Schema(description = "Name of the user")
    String name;

    @Schema(description = "Identifier for the user")
    String userId;

    @Schema(description = "URL of the server which user prefers for conducting the conference ")
    String preferredServerUrl;
}
