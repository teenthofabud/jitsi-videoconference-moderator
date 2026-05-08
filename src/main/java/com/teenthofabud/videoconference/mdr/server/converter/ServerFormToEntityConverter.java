package com.teenthofabud.videoconference.mdr.server.converter;

import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.server.form.ServerForm;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ServerFormToEntityConverter implements Converter<ServerForm, ServerEntity> {

    @Override
    public ServerEntity convert(ServerForm source) {
        return ServerEntity.builder()
                .name(source.getName())
                .serverUrl(source.getServerUrl())
                .build();

    }
}
