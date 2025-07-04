package com.teenthofabud.videoconference.mdr.conference.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import com.teenthofabud.videoconference.mdr.conference.constants.google.GoogleConferenceErrorType;
import com.teenthofabud.videoconference.mdr.conference.converter.ConferenceEntityToConferenceVoConverter;
import com.teenthofabud.videoconference.mdr.conference.entity.ConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.entity.google.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.reducer.GoogleConferenceEntityToGoogleConferenceDataVoReducer;
import com.teenthofabud.videoconference.mdr.conference.reducer.GoogleConferenceFormToGoogleConferenceEntityReducer;
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
    @Autowired
    GoogleConferenceFormToGoogleConferenceEntityReducer googleConferenceFormToGoogleConferenceEntityReducer;
    @Autowired
    GoogleConferenceEntityToGoogleConferenceDataVoReducer googleConferenceEntityToGoogleConferenceDataVoReducer;

    @Override
    public GoogleConferenceDataVo createNewGoogleConference(GoogleConferenceForm form) throws GoogleConferenceException {
        String calendarId = form.getCalendarEvent().getCalendarId();
        ServerPreferenceVo serverPreferenceVo = null;
        try {
            serverPreferenceVo = serverPreferenceService.getServerPreferenceByUserId(calendarId);
        } catch (ServerPreferenceException e) {
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY, MDRErrorCode.MDR_ATTRIBUTE_INVALID,new Object[]{calendarId});
        }
        GoogleConferenceEntity conferenceEntity = googleConferenceFormToGoogleConferenceEntityReducer.reduce(form,serverPreferenceVo);
        googleConferenceRepository.save(conferenceEntity);
        GoogleConferenceDataVo googleConferenceDataVo = googleConferenceEntityToGoogleConferenceDataVoReducer.reduce(conferenceEntity,serverPreferenceVo);
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
