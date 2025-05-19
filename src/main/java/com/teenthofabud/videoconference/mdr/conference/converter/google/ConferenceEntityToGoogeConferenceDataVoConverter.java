package com.teenthofabud.videoconference.mdr.conference.converter.google;

import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.entity.ConferenceEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ConferenceEntityToGoogeConferenceDataVoConverter implements Converter<ConferenceEntity, GoogleConferenceDataVo> {

    @Override
    public GoogleConferenceDataVo convert(ConferenceEntity source) {
        return GoogleConferenceDataVo.builder()
                .id(source.getRoomId())
                .adminEmail(source.getUserId())
                .videoUri(source.getServerUrl())
                .build();
    }
}
