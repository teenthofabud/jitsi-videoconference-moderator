package com.teenthofabud.videoconference.mdr.conference.outlook.vo;

import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OutlookConferenceSetupVo {

    Long userId;

    List<ServerVo> servers;
}
