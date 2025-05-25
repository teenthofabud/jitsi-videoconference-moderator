package com.teenthofabud.videoconference.mdr.conference.converter;

import com.teenthofabud.videoconference.mdr.conference.dto.ConferenceDto;
import com.teenthofabud.videoconference.mdr.conference.entity.ConferenceEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ConferenceEntityToConferenceDtoConverter implements Converter<ConferenceEntity, ConferenceDto> {
    @Override
    public ConferenceDto convert(ConferenceEntity source) {
        return ConferenceDto.builder()
                .roomId(source.getRoomId())
                .userId(source.getUserId())
                .serverUrl(source.getServerUrl())
                .status(source.getStatus())
                .build();
    }
}
