package com.teenthofabud.videoconference.mdr.server.vo;

import com.teenthofabud.videoconference.mdr.serverpreference.vo.ServerPreferenceVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ServerVo {

    @Schema(description = "Server ID (PK)")
    Long id;

    @Schema(description = "Server name")
    String name;

    @Schema(description = "Base Url for the server")
    String serverUrl;

    @Schema(description = "Server Preference list")
    List<ServerPreferenceVo> serverPreferences;

}
