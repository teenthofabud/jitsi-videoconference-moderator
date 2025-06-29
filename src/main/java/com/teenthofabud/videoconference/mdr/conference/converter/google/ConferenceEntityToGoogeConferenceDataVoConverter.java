package com.teenthofabud.videoconference.mdr.conference.converter.google;

import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.entity.ConferenceEntity;
import com.teenthofabud.videoconference.mdr.serverpreference.error.ServerPreferenceException;
import com.teenthofabud.videoconference.mdr.serverpreference.service.ServerPreferenceService;
import com.teenthofabud.videoconference.mdr.serverpreference.vo.ServerPreferenceVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ConferenceEntityToGoogeConferenceDataVoConverter implements Converter<ConferenceEntity, Optional<GoogleConferenceDataVo>> {

    @Autowired
    ServerPreferenceService serverPreferenceService;

    @Override
    public Optional<GoogleConferenceDataVo> convert(ConferenceEntity source) {
        try {
            ServerPreferenceVo serverPreferenceVo = serverPreferenceService.getServerPreferenceById(source.getServerPreferenceId());
            return Optional.of(GoogleConferenceDataVo.builder()
                    .id(source.getRoomId())
                    .adminEmail(serverPreferenceVo.getUserId())
                    .videoUri(String.format("%s/%s",serverPreferenceVo.getServerUrl(), source.getRoomId()))
                    .build());
        } catch (ServerPreferenceException e) {
            return Optional.empty();
        }

    }
}
