package com.teenthofabud.videoconference.mdr.server.converter;

import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.serverpreference.converter.ServerPreferenceEntityToVoConverter;
import com.teenthofabud.videoconference.mdr.serverpreference.vo.ServerPreferenceVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ServerEntityToVoConverter implements Converter<ServerEntity, ServerVo> {

    @Autowired
    ServerPreferenceEntityToVoConverter serverPreferenceEntityToVoConverter;

    @Override
    public ServerVo convert(ServerEntity source) {

        return ServerVo.builder()
                .name(source.getName())
                .id(source.getId())
                .serverUrl(source.getServerUrl())
                .build();
    }
}
