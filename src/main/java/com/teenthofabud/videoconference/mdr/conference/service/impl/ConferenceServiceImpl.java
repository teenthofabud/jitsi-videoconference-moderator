package com.teenthofabud.videoconference.mdr.conference.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import com.teenthofabud.videoconference.mdr.conference.constants.google.GoogleConferenceErrorType;
import com.teenthofabud.videoconference.mdr.conference.converter.ConferenceEntityToConferenceVoConverter;
import com.teenthofabud.videoconference.mdr.conference.entity.ConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.entity.google.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.repository.google.GoogleConferenceRepository;
import com.teenthofabud.videoconference.mdr.conference.vo.ConferenceVo;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleCalendarEvent;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.service.ConferenceService;
import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.serverpreference.vo.ServerPreferenceVo;
import com.teenthofabud.videoconference.mdr.serverpreference.error.ServerPreferenceException;
import com.teenthofabud.videoconference.mdr.serverpreference.service.ServerPreferenceService;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConferenceServiceImpl implements ConferenceService {

    @Autowired
    GoogleConferenceRepository googleConferenceRepository;
    @Autowired
    ConferenceEntityToConferenceVoConverter conferenceEntityToConferenceVoConverter;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ServerPreferenceService serverPreferenceService;

    @Override
    public GoogleConferenceDataVo createNewGoogleConference(GoogleConferenceForm form) throws GoogleConferenceException {
        String calendarId = form.getCalendarEvent().getCalendarId();
        String eventId = form.getCalendarEvent().getEventId();
        ServerPreferenceVo serverPreferenceVo = null;
        try {
            serverPreferenceVo = serverPreferenceService.getServerPreferenceByUserId(calendarId);
        } catch (ServerPreferenceException e) {
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY, MDRErrorCode.MDR_ATTRIBUTE_INVALID,new Object[]{calendarId});
        }
        String uuidInput = String.format("%s:%s:%s",calendarId,serverPreferenceVo.getServerUrl(),Long.toString(System.currentTimeMillis()));
        UUID conferenceId = UUID.nameUUIDFromBytes(uuidInput.getBytes());
        GoogleConferenceEntity conferenceEntity = GoogleConferenceEntity.builder()
                .serverPreferenceId(serverPreferenceVo.getId())
                .roomId(conferenceId.toString())
                .eventId(eventId)
                .status(ConferenceStatus.ACTIVE)
                .build();
        googleConferenceRepository.save(conferenceEntity);
        GoogleConferenceDataVo googleConferenceDataVo = GoogleConferenceDataVo.builder()
                .adminEmail(calendarId)
                .eventId(eventId)
                .videoUri(String.format("%s/%s",serverPreferenceVo.getServerUrl(),conferenceEntity.getRoomId()))
                .id(conferenceId.toString())
                .build();
        return googleConferenceDataVo;
    }

    @Override
    public ConferenceVo getGoogleConferenceById(Long conferenceId) throws GoogleConferenceException {
        Optional<GoogleConferenceEntity> optionalConferenceEntity = googleConferenceRepository.findById(conferenceId);
        ConferenceVo conferenceVo = null;
        if(optionalConferenceEntity.isEmpty())
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,MDRErrorCode.MDR_NOT_FOUND,new Object[]{conferenceId});
        else{
            ConferenceEntity conferenceEntity = optionalConferenceEntity.get();
            try {
                ServerPreferenceVo serverPreferenceVo = serverPreferenceService.getServerPreferenceById(conferenceEntity.getServerPreferenceId());
                conferenceVo = conferenceEntityToConferenceVoConverter.convert(optionalConferenceEntity.get());
            } catch (ServerPreferenceException e) {
                throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,MDRErrorCode.MDR_ATTRIBUTE_INVALID,new Object[]{conferenceEntity.getServerPreferenceId()});
            }
        }
        return conferenceVo;
    }

    @Override
    public List<ConferenceVo> getAllGoogleConferencesByUserId(String userId) throws GoogleConferenceException {
        ServerPreferenceVo serverPreferenceVo = null;
        try {
            serverPreferenceVo = serverPreferenceService.getServerPreferenceByUserId(userId);
        } catch (ServerPreferenceException e) {
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,MDRErrorCode.MDR_ATTRIBUTE_INVALID,new Object[]{userId});
        }
        List<ConferenceVo> conferenceVoList = googleConferenceRepository.findByServerPreferenceId(serverPreferenceVo.getId())
                .stream()
                .map(e-> conferenceEntityToConferenceVoConverter.convert(e))
                .collect(Collectors.toList());
        return conferenceVoList;
    }

    @Override
    public void patchGoogleConferenceById(Long conferenceId, GoogleCalendarEvent calendarEvent) {
        //TO-DECIDE the parameters involved
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void deleteGoogleConferenceById(Long conferenceId) throws GoogleConferenceException {
        Optional<GoogleConferenceEntity> optionalConferenceEntity = googleConferenceRepository.findById(conferenceId);
        if(optionalConferenceEntity.isEmpty())
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,MDRErrorCode.MDR_NOT_FOUND,new Object[]{conferenceId});
        else googleConferenceRepository.deleteById(conferenceId);

    }
}
