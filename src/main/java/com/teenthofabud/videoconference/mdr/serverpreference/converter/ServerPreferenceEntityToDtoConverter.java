package com.teenthofabud.videoconference.mdr.serverpreference.converter;

import com.teenthofabud.videoconference.mdr.serverpreference.dto.ServerPreferenceDto;
import com.teenthofabud.videoconference.mdr.serverpreference.entity.ServerPreferenceEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ServerPreferenceEntityToDtoConverter implements Converter<ServerPreferenceEntity, ServerPreferenceDto> {
    @Override
    public ServerPreferenceDto convert(ServerPreferenceEntity source) {
        return ServerPreferenceDto.builder()
                .userId(source.getUserId())
                .serverUrl(source.getServerUrl())
                .build();
    }
}
