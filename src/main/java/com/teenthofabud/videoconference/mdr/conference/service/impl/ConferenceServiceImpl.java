package com.teenthofabud.videoconference.mdr.conference.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
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
        String eventId = form.getEventData().getEventId();
        String userIdHash = DigestUtils.md5DigestAsHex(calendarId.getBytes(StandardCharsets.UTF_8));
        ServerPreferenceVo serverPreferenceVo = null;
        try {
            serverPreferenceVo = serverPreferenceService.getServerPreferenceByUserIdHash(userIdHash);
        } catch (ServerPreferenceException e) {
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY, MDRErrorCode.MDR_ATTRIBUTE_INVALID,new Object[]{calendarId});
        }
        GoogleConferenceEntity conferenceEntity = null;
        Optional<GoogleConferenceEntity> optionalGoogleConferenceEntity = googleConferenceRepository.findByServerPreferenceIdAndEventId(serverPreferenceVo.getId(),eventId);
        if(optionalGoogleConferenceEntity.isEmpty()){
            conferenceEntity = googleConferenceFormToGoogleConferenceEntityReducer.reduce(form,serverPreferenceVo);
            googleConferenceRepository.save(conferenceEntity);
        }
        else
            conferenceEntity = optionalGoogleConferenceEntity.get();
        GoogleConferenceDataVo googleConferenceDataVo = googleConferenceEntityToGoogleConferenceDataVoReducer.reduce(conferenceEntity,serverPreferenceVo.getServerUrl(),calendarId);
        return googleConferenceDataVo;
    }

    @Override
    public ConferenceVo getGoogleConferenceByUserAndEventId(String eventId, String userId) throws GoogleConferenceException {
        String userIdHash = DigestUtils.md5DigestAsHex(userId.getBytes(StandardCharsets.UTF_8));
        ConferenceVo conferenceVo = null;
        ServerPreferenceVo serverPreferenceVo = null;
        try {
            serverPreferenceVo = serverPreferenceService.getServerPreferenceByUserIdHash(userIdHash);
        } catch (ServerPreferenceException e) {
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,MDRErrorCode.MDR_ATTRIBUTE_INVALID,new Object[]{userId});
        }
        Optional<GoogleConferenceEntity> optionalConferenceEntity = googleConferenceRepository.findByServerPreferenceIdAndEventId(serverPreferenceVo.getId(), eventId);
        if(optionalConferenceEntity.isEmpty())
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,MDRErrorCode.MDR_NOT_FOUND,new Object[]{userId});
        else{
            conferenceVo = conferenceEntityToConferenceVoConverter.convert(optionalConferenceEntity.get());
        }
        return conferenceVo;
    }

    @Override
    public List<ConferenceVo> getAllGoogleConferencesByUserId(String userId) throws GoogleConferenceException {

        ServerPreferenceVo serverPreferenceVo = null;
        String userIdHash = DigestUtils.md5DigestAsHex(userId.getBytes(StandardCharsets.UTF_8));
        try {
            serverPreferenceVo = serverPreferenceService.getServerPreferenceByUserIdHash(userIdHash);
        } catch (ServerPreferenceException e) {
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,MDRErrorCode.MDR_ATTRIBUTE_INVALID,new Object[]{userId});
        }
        List<ConferenceVo> conferenceVoList = googleConferenceRepository.findAllByServerPreferenceId(serverPreferenceVo.getId())
                .stream()
                .map(e-> conferenceEntityToConferenceVoConverter.convert(e))
                .collect(Collectors.toList());
        return conferenceVoList;
    }

    @Override
    public void patchGoogleConferenceByUserAndEventId(String eventId, String userId, JsonPatch patches) throws GoogleConferenceException, JsonPatchException, JsonProcessingException {
        String userIdHash = DigestUtils.md5DigestAsHex(userId.getBytes(StandardCharsets.UTF_8));
        ServerPreferenceVo serverPreferenceVo = null;
        try {
            serverPreferenceVo = serverPreferenceService.getServerPreferenceByUserIdHash(userIdHash);
        } catch (ServerPreferenceException e) {
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,MDRErrorCode.MDR_ATTRIBUTE_INVALID,new Object[]{userId});
        }
        Optional<GoogleConferenceEntity> optionalConferenceEntity = googleConferenceRepository.findByServerPreferenceIdAndEventId(serverPreferenceVo.getId(), eventId);
        if(optionalConferenceEntity.isEmpty())
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,MDRErrorCode.MDR_NOT_FOUND,new Object[]{userId});
        GoogleConferenceEntity conferenceEntity = optionalConferenceEntity.get();
        JsonNode jsonNode = objectMapper.valueToTree(conferenceEntity);
        log.info("Before patch : {}",objectMapper.writeValueAsString(conferenceEntity));
        JsonNode patchedNode = patches.apply(jsonNode);
        conferenceEntity = objectMapper.treeToValue(patchedNode,GoogleConferenceEntity.class);
        log.info("after patching : {}",conferenceEntity);
        googleConferenceRepository.save(conferenceEntity);
    }

    @Override
    public void deleteGoogleConferenceByUserAndEventId(String eventId, String userId) throws GoogleConferenceException {
        String userIdHash = DigestUtils.md5DigestAsHex(userId.getBytes(StandardCharsets.UTF_8));
        ConferenceVo conferenceVo = null;
        ServerPreferenceVo serverPreferenceVo = null;
        try {
            serverPreferenceVo = serverPreferenceService.getServerPreferenceByUserIdHash(userIdHash);
        } catch (ServerPreferenceException e) {
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,MDRErrorCode.MDR_ATTRIBUTE_INVALID,new Object[]{userIdHash});
        }
        Optional<GoogleConferenceEntity> optionalConferenceEntity = googleConferenceRepository.findByServerPreferenceIdAndEventId(serverPreferenceVo.getId(),eventId);
        if(optionalConferenceEntity.isEmpty())
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,MDRErrorCode.MDR_NOT_FOUND,new Object[]{userId});
        else googleConferenceRepository.deleteById(optionalConferenceEntity.get().getId());

    }
}
