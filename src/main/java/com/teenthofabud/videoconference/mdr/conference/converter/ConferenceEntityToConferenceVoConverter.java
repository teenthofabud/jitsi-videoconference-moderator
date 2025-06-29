package com.teenthofabud.videoconference.mdr.conference.converter;

import com.teenthofabud.videoconference.mdr.conference.vo.ConferenceVo;
import com.teenthofabud.videoconference.mdr.conference.entity.ConferenceEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ConferenceEntityToConferenceVoConverter implements Converter<ConferenceEntity, ConferenceVo> {
    @Override
    public ConferenceVo convert(ConferenceEntity source) {
        return ConferenceVo.builder()
                .roomId(source.getRoomId())
                .serverPreferenceId(source.getServerPreferenceId())
                .status(source.getStatus())
                .build();
    }
}
