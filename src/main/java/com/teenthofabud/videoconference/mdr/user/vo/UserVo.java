package com.teenthofabud.videoconference.mdr.user.vo;

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

    @Schema(description = "Whether the user is a guest (isManaged = false) or signed up user")
    Boolean isManaged;

    @Schema(description = "Optimistic locking version")
    Long version;

    @Schema(description = "Date and time the entity was last modified")
    java.time.Instant lastModifiedDate;

}
