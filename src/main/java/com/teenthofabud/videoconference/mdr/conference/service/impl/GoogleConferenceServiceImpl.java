package com.teenthofabud.videoconference.mdr.conference.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teenthofabud.videoconference.mdr.conference.constants.google.GoogleConferenceErrorType;
import com.teenthofabud.videoconference.mdr.conference.converter.ConferenceEntityToConferenceVoConverter;
import com.teenthofabud.videoconference.mdr.conference.converter.google.GoogleConferenceEntityToGoogleConferenceVoConverter;
import com.teenthofabud.videoconference.mdr.conference.entity.google.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.reducer.GoogleConferenceEntityToGoogleConferenceDataVoReducer;
import com.teenthofabud.videoconference.mdr.conference.reducer.GoogleConferenceFormToGoogleConferenceEntityReducer;
import com.teenthofabud.videoconference.mdr.conference.repository.google.GoogleConferenceRepository;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.service.GoogleConferenceService;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceVo;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.service.ServerService;
import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import com.teenthofabud.videoconference.mdr.user.vo.UserVo;
import jakarta.json.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleConferenceServiceImpl implements GoogleConferenceService {

    private final GoogleConferenceRepository googleConferenceRepository;
    private final ConferenceEntityToConferenceVoConverter conferenceEntityToConferenceVoConverter;
    private final GoogleConferenceEntityToGoogleConferenceVoConverter googleConferenceEntityToGoogleConferenceVoConverter;
    private final ObjectMapper objectMapper;
    private final GoogleConferenceFormToGoogleConferenceEntityReducer googleConferenceFormToGoogleConferenceEntityReducer;
    private final GoogleConferenceEntityToGoogleConferenceDataVoReducer googleConferenceEntityToGoogleConferenceDataVoReducer;
    private final UserService userService;
    private final ServerService serverService;

    @Override
    @Transactional
    public GoogleConferenceDataVo createNewGoogleConference(GoogleConferenceForm form) throws GoogleConferenceException {
        Long userId = form.getUserId();
        String eventId = form.getEventData().getEventId();
        UserVo userVo;
        ServerVo serverVo;
        try{
            userVo = userService.getUserById(userId);
        }
        catch (UserException e) {
            log.error("User with userId {} not found",userId);
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,String.format("User with userId %s not found",userId));
        }
        try{
            serverVo = serverService.getServerById(form.getServerId());
        }
        catch (ServerException e){
            log.error("Server with serverID {} not found",form.getServerId());
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY, String.format("Server with ID %s not found",form.getServerId()));
        }
        Optional<GoogleConferenceEntity> optionalGoogleConferenceEntity = googleConferenceRepository.findByUserIdAndEventId(userId,eventId);
        GoogleConferenceEntity conferenceEntity = optionalGoogleConferenceEntity.orElseGet(()->{
            GoogleConferenceEntity newEntity = googleConferenceFormToGoogleConferenceEntityReducer.reduce(form,userVo);
            return googleConferenceRepository.save(newEntity);
        });
        GoogleConferenceDataVo googleConferenceDataVo = googleConferenceEntityToGoogleConferenceDataVoReducer.reduce(conferenceEntity,userId, serverVo.getServerUrl());
        return googleConferenceDataVo;
    }

    @Override
    @Transactional
    public GoogleConferenceVo getGoogleConferenceByUserIdAndEventId(Long userId, String eventId) throws GoogleConferenceException {
        GoogleConferenceVo conferenceVo = null;
        UserVo userVo;
        try{
            userVo = userService.getUserById(userId);
        }
        catch (UserException e) {
            log.error("User with userId {} not found",userId);
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,String.format("User with userId %s not found",userId));
        }
        Optional<GoogleConferenceEntity> optionalConferenceEntity = googleConferenceRepository.findByUserIdAndEventId(userId,eventId);
        if(optionalConferenceEntity.isEmpty())
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,String.format("Calendar event with ID %s not found",eventId));
        else{
            conferenceVo = googleConferenceEntityToGoogleConferenceVoConverter.convert(optionalConferenceEntity.get());
        }
        return conferenceVo;
    }

    @Override
    @Transactional
    public List<GoogleConferenceVo> getAllGoogleConferencesByUserId(Long userId) throws GoogleConferenceException {
        UserVo userVo;
        try{
            userVo = userService.getUserById(userId);
        }
        catch (UserException e) {
            log.error("User with userId {} not found",userId);
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY, String.format("User with userId %s not found",userId));
        }
        List<GoogleConferenceVo> conferenceVoList = googleConferenceRepository.findAllByUserId(userId)
                .stream()
                .map(e-> googleConferenceEntityToGoogleConferenceVoConverter.convert(e))
                .collect(Collectors.toList());
        return conferenceVoList;
    }

    @Override
    @Transactional
    public void patchGoogleConferenceByUserIdAndEventId(Long userId, String eventId, String rawPatches) throws GoogleConferenceException, JsonProcessingException {
        UserVo userVo;
        try{
            userVo = userService.getUserById(userId);
        }
        catch (UserException e) {
            log.error("User with userId {} not found",userId);
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY, String.format("User with userId %s not found",userId));
        }
        Optional<GoogleConferenceEntity> optionalConferenceEntity = googleConferenceRepository.findByUserIdAndEventId(userId,eventId);
        if(optionalConferenceEntity.isEmpty())
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,String.format("Calendar event with ID %s not found",eventId));
        GoogleConferenceEntity conferenceEntity = optionalConferenceEntity.get();
        JsonArray patchArray;
        try (JsonReader reader = Json.createReader(new StringReader(rawPatches))) {
            patchArray = reader.readArray();
        }
        JsonPatch patches = Json.createPatch(patchArray);
        // POJO -> String (Jackson) -> JsonObject (Jakarta)
        String entityAsJsonString = objectMapper.writeValueAsString(conferenceEntity);
        JsonObject source;
        try (JsonReader reader = Json.createReader(new StringReader(entityAsJsonString))) {
            source = reader.readObject();
        }
        // Apply the patch natively in Jakarta
        JsonObject patched = patches.apply(source);
        // JsonObject -> String (Jakarta) -> POJO (Jackson)
        String patchedJsonString = patched.toString();
        conferenceEntity = objectMapper.readValue(patchedJsonString, GoogleConferenceEntity.class);
        log.debug("after patching : {}",conferenceEntity);
        googleConferenceRepository.save(conferenceEntity);
    }

    @Override
    @Transactional
    public void deleteGoogleConferenceByUserIdAndEventId(Long userId, String eventId) throws GoogleConferenceException {
        UserVo userVo;
        try{
            userVo = userService.getUserById(userId);
        }
        catch (UserException e) {
            log.error("User with userId {} not found",userId);
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY, String.format("User with userId %s not found",userId));
        }
        Optional<GoogleConferenceEntity> optionalConferenceEntity = googleConferenceRepository.findByUserIdAndEventId(userId,eventId);
        if(optionalConferenceEntity.isEmpty())
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,String.format("Calendar event with ID %s not found",eventId));
        else googleConferenceRepository.deleteById(optionalConferenceEntity.get().getId());
    }

}
