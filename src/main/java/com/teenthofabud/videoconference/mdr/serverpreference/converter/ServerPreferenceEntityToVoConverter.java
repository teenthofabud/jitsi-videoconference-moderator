package com.teenthofabud.videoconference.mdr.serverpreference.converter;

import com.teenthofabud.videoconference.mdr.serverpreference.vo.ServerPreferenceVo;
import com.teenthofabud.videoconference.mdr.serverpreference.entity.ServerPreferenceEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ServerPreferenceEntityToVoConverter implements Converter<ServerPreferenceEntity, ServerPreferenceVo> {
    @Override
    public ServerPreferenceVo convert(ServerPreferenceEntity source) {
        return ServerPreferenceVo.builder()
                .userId(source.getUserId())
                .id(source.getId())
                .serverUrl(source.getServerUrl())
                .serverPreferenceType(source.getServerPreferenceType())
                .build();
    }
}
