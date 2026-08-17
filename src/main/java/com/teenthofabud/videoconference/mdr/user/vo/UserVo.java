package com.teenthofabud.videoconference.mdr.user.vo;

import com.teenthofabud.videoconference.mdr.user.constants.ReferenceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserVo {

    @Schema(description = "user ID (PK)")
    Long id;

    @Schema(description = "Email of the user who created the conference")
    String email;

    @Schema(description = "Name of the user")
    String name;

    @Schema(description = "URL for the preferred server")
    String preferredServerUrl;

//    @Schema(description = "User reference type, eg:- GMAIL")
//    ReferenceType referenceType;

}
