package com.teenthofabud.videoconference.moderator.conference.converter;

import com.teenthofabud.videoconference.moderator.conference.dto.ConferenceDto;
import com.teenthofabud.videoconference.moderator.conference.entity.ConferenceEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ConferenceEntityToDtoConverter implements Converter<ConferenceEntity, ConferenceDto> {

    @Override
    public ConferenceDto convert(ConferenceEntity source) {
        return ConferenceDto.builder()
                .roomId(source.getRoomId())
                .userRef(source.getUserRef())
                .status(source.getStatus())
                .serverUrl(source.getServerUrl())
                .build();
    }
}
