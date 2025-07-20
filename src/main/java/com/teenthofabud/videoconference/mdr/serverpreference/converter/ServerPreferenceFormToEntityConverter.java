package com.teenthofabud.videoconference.mdr.serverpreference.converter;

import com.teenthofabud.videoconference.mdr.serverpreference.constants.ServerPreferenceType;
import com.teenthofabud.videoconference.mdr.serverpreference.entity.ServerPreferenceEntity;
import com.teenthofabud.videoconference.mdr.serverpreference.form.ServerPreferenceForm;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ServerPreferenceFormToEntityConverter implements Converter<ServerPreferenceForm, ServerPreferenceEntity> {
    @Override
    public ServerPreferenceEntity convert(ServerPreferenceForm source) {
        return ServerPreferenceEntity.builder()
                .serverUrl(source.getServerUrl())
                .serverPreferenceType(ServerPreferenceType.valueOf(source.getServerPreferenceType()))
                .userIdHash(source.getUserIdHash())
                .build();
    }
}
