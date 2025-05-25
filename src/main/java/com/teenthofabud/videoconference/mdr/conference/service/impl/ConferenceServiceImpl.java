package com.teenthofabud.videoconference.mdr.conference.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.teenthofabud.videoconference.mdr.conference.constants.google.GoogleConferenceErrorType;
import com.teenthofabud.videoconference.mdr.conference.converter.ConferenceEntityToConferenceDtoConverter;
import com.teenthofabud.videoconference.mdr.conference.converter.google.ConferenceEntityToGoogeConferenceDataVoConverter;
import com.teenthofabud.videoconference.mdr.conference.dto.ConferenceDto;
import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleCalendarEvent;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.entity.ConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.repository.ConferenceRepository;
import com.teenthofabud.videoconference.mdr.conference.service.ConferenceService;
import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConferenceServiceImpl implements ConferenceService {

    @Autowired
    ConferenceRepository conferenceRepository;
    @Autowired
    ConferenceEntityToGoogeConferenceDataVoConverter conferenceEntityToGoogeConferenceDataVoConverter;
    @Autowired
    ConferenceEntityToConferenceDtoConverter conferenceEntityToConferenceDtoConverter;
    @Autowired
    ObjectMapper objectMapper;

    @Override
    public GoogleConferenceDataVo createNewGoogleConference(GoogleConferenceForm form) throws GoogleConferenceException, GoogleConferenceException {
        GoogleConferenceDataVo googleConferenceDataVo = GoogleConferenceDataVo.builder().build();
        String calendarId = form.getCalendarEvent().getCalendarId();
        

//        if(conferenceEntity!=null){
//            googleConferenceDataVo = conferenceEntityToGoogeConferenceDataVoConverter.convert(conferenceEntity);
//        }
//        else
//            throw new GoogleConferenceException(GoogleConferenceErrorType.CONFERENCE_SOLUTION_FORBIDDEN,new Object[]{calendarId});
        return googleConferenceDataVo;
    }

    @Override
    public ConferenceDto getConferenceById(Long conferenceId) throws ConferenceException {
        Optional<ConferenceEntity> optionalConferenceEntity = conferenceRepository.findById(conferenceId);
        ConferenceDto conferenceDto = null;
        if(optionalConferenceEntity.isEmpty())
            throw new ConferenceException(MDRErrorCode.MDR_NOT_FOUND,new Object[]{conferenceId});
        else
            conferenceDto = conferenceEntityToConferenceDtoConverter.convert(optionalConferenceEntity.get());
        return conferenceDto;
    }

    @Override
    public List<ConferenceDto> getAllConferencesByUserId(String userId) {
        List<ConferenceDto> conferenceDtoList = conferenceRepository.findByUserId(userId)
                .stream()
                .map(e->conferenceEntityToConferenceDtoConverter.convert(e))
                .collect(Collectors.toList());
        return List.of();
    }

    @Override
    public void patchGoogleConferenceById(Long conferenceId, GoogleCalendarEvent calendarEvent) {
        //TO-DECIDE the parameters involved
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void patchConferenceById(Long conferenceId, JsonPatch patches) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void deleteConferenceById(Long conferenceId) throws ConferenceException {
        Optional<ConferenceEntity> optionalConferenceEntity = conferenceRepository.findById(conferenceId);
        ConferenceDto conferenceDto = null;
        if(optionalConferenceEntity.isEmpty())
            throw new ConferenceException(MDRErrorCode.MDR_NOT_FOUND,new Object[]{conferenceId});
        else conferenceRepository.deleteById(conferenceId);

    }
}
