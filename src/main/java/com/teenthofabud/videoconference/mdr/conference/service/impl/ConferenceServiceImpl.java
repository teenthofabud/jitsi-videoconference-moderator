package com.teenthofabud.videoconference.mdr.conference.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import com.teenthofabud.videoconference.mdr.conference.converter.ConferenceEntityToConferenceVoConverter;
import com.teenthofabud.videoconference.mdr.conference.converter.google.ConferenceEntityToGoogeConferenceDataVoConverter;
import com.teenthofabud.videoconference.mdr.conference.entity.google.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.repository.google.GoogleConferenceRepository;
import com.teenthofabud.videoconference.mdr.conference.vo.ConferenceVo;
import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleCalendarEvent;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.entity.ConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.service.ConferenceService;
import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.serverpreference.vo.ServerPreferenceVo;
import com.teenthofabud.videoconference.mdr.serverpreference.error.ServerPreferenceException;
import com.teenthofabud.videoconference.mdr.serverpreference.service.ServerPreferenceService;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
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
    ConferenceEntityToGoogeConferenceDataVoConverter conferenceEntityToGoogeConferenceDataVoConverter;
    @Autowired
    ConferenceEntityToConferenceVoConverter conferenceEntityToConferenceVoConverter;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ServerPreferenceService serverPreferenceService;
    @Autowired
    Validator validator;

    @Override
    public GoogleConferenceDataVo createNewGoogleConference(GoogleConferenceForm form) throws GoogleConferenceException, UserException, ServerPreferenceException {
        log.info("{}",form);
        String calendarId = form.getCalendarEvent().getCalendarId();
        String eventId = form.getCalendarEvent().getEventId();
        ServerPreferenceVo serverPreferenceVo = serverPreferenceService.getServerPreferenceByUserId(calendarId);
        String uuidInput = String.format("%s%s%s",calendarId,serverPreferenceVo.getServerUrl(),Long.toString(System.currentTimeMillis()));
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
    public ConferenceVo getGoogleConferenceById(Long conferenceId) throws ConferenceException {
        Optional<GoogleConferenceEntity> optionalConferenceEntity = googleConferenceRepository.findById(conferenceId);
        ConferenceVo conferenceVo = null;
        if(optionalConferenceEntity.isEmpty())
            throw new ConferenceException(MDRErrorCode.MDR_NOT_FOUND,new Object[]{conferenceId});
        else
            conferenceVo = conferenceEntityToConferenceVoConverter.convert(optionalConferenceEntity.get());
        return conferenceVo;
    }

    @Override
    public List<ConferenceVo> getAllConferencesByUserId(String userId) throws ServerPreferenceException {
        ServerPreferenceVo serverPreferenceVo = serverPreferenceService.getServerPreferenceByUserId(userId);
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
    public void deleteGoogleConferenceById(Long conferenceId) throws ConferenceException {
        Optional<GoogleConferenceEntity> optionalConferenceEntity = googleConferenceRepository.findById(conferenceId);
        if(optionalConferenceEntity.isEmpty())
            throw new ConferenceException(MDRErrorCode.MDR_NOT_FOUND,new Object[]{conferenceId});
        else googleConferenceRepository.deleteById(conferenceId);

    }
}
