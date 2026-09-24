package com.teenthofabud.videoconference.mdr.conference.google.service.impl;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonPatch;
import jakarta.json.JsonReader;
import com.teenthofabud.videoconference.mdr.conference.google.constants.GoogleConferenceErrorType;
import com.teenthofabud.videoconference.mdr.conference.converter.ConferenceEntityToConferenceVoConverter;
import com.teenthofabud.videoconference.mdr.conference.google.converter.GoogleConferenceEntityToGoogleConferenceVoConverter;
import com.teenthofabud.videoconference.mdr.conference.google.entity.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.google.form.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.google.reducer.GoogleConferenceEntityToGoogleConferenceDataVoReducer;
import com.teenthofabud.videoconference.mdr.conference.google.reducer.GoogleConferenceFormToGoogleConferenceEntityReducer;
import com.teenthofabud.videoconference.mdr.conference.google.repository.GoogleConferenceRepository;
import com.teenthofabud.videoconference.mdr.conference.google.error.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.google.vo.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.google.service.GoogleConferenceService;
import com.teenthofabud.videoconference.mdr.conference.google.vo.GoogleConferenceVo;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.service.ServerService;
import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import com.teenthofabud.videoconference.mdr.user.vo.UserVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.io.StringReader;
import java.util.List;
import java.util.Objects;
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
    public void patchGoogleConferenceByUserIdAndEventId(Long userId, String eventId, JsonPatch patches) throws GoogleConferenceException {
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
        String before = objectMapper.writeValueAsString(conferenceEntity);
        log.info("Before patch : {}",before);
        JsonObject patchedObject = patches.apply(readObject(before));
        GoogleConferenceEntity patchedEntity = objectMapper.readValue(patchedObject.toString(),GoogleConferenceEntity.class);
        rejectIdentityChanges(conferenceEntity, patchedEntity);
        log.info("after patching : {}",patchedEntity);
        googleConferenceRepository.save(patchedEntity);
    }

    private JsonObject readObject(String json){
        try(JsonReader reader = Json.createReader(new StringReader(json))){
            return reader.readObject();
        }
    }

    /**
     * Rewriting the primary key, the calendar event it belongs to, or the organizer who owns it
     * would orphan a live meeting or hand the conference to another user, so those are refused.
     */
    private void rejectIdentityChanges(GoogleConferenceEntity original, GoogleConferenceEntity patched) throws GoogleConferenceException {
        if(Objects.equals(original.getId(), patched.getId())
                && Objects.equals(original.getEventId(), patched.getEventId())
                && Objects.equals(original.getUserId(), patched.getUserId())){
            return;
        }
        log.error("Patch attempted to change immutable attributes of the conference with event id {}", original.getEventId());
        throw new GoogleConferenceException(GoogleConferenceErrorType.PERMANENT,
                "Patch attempted to change immutable attributes of the conference");
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
