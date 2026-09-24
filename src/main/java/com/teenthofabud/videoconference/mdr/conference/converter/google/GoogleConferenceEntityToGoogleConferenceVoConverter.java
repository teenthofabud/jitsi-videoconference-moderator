package com.teenthofabud.videoconference.mdr.conference.converter.google;

import com.teenthofabud.videoconference.mdr.conference.entity.google.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceVo;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class GoogleConferenceEntityToGoogleConferenceVoConverter implements Converter<GoogleConferenceEntity, GoogleConferenceVo> {
    @Override
    public GoogleConferenceVo convert(GoogleConferenceEntity source) {
        return GoogleConferenceVo.builder()
                .eventId(source.getEventId())
                .roomId(source.getRoomId())
                .status(source.getStatus())
                .serverId(source.getServerId())
                .version(source.getVersion())
                .lastModifiedDate(source.getLastModifiedDate())
                .build();

    }
}
