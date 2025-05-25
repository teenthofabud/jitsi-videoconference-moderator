package com.teenthofabud.videoconference.mdr.server.converter;

import com.teenthofabud.videoconference.mdr.server.dto.ServerDto;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ServerEntityToDtoConverter implements Converter<ServerEntity, ServerDto> {
    @Override
    public ServerDto convert(ServerEntity source) {
        return ServerDto.builder()
                .name(source.getName())
                .serverUrl(source.getServerUrl())
                .build();
    }
}
