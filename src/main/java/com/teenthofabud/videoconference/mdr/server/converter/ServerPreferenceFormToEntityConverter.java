package com.teenthofabud.videoconference.mdr.server.converter;

import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.server.form.ServerPreferenceForm;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ServerPreferenceFormToEntityConverter implements Converter<ServerPreferenceForm, ServerEntity> {

    @Override
    public ServerEntity convert(ServerPreferenceForm source) {
        return ServerEntity.builder()
                .name(source.getName())
                .serverUrl(source.getServerUrl())
                .userId(source.getUserId())
                .build();

    }

}
