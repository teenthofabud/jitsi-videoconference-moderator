package com.teenthofabud.videoconference.mdr.conference.service.impl;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonPatch;
import jakarta.json.JsonReader;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
        String email = form.getEventData().getCalendarId();
        String eventId = form.getEventData().getEventId();
        UserVo userVo;
        ServerVo serverVo;
        try{
            userVo = userService.getUserByEmail(email);
        }
        catch (UserException e) {
            log.error("User with calendarID/email {} not found",email);
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,String.format("User with calendarId/email %s not found",email));
        }
        try{
            serverVo = serverService.getServerById(form.getServerId());
        }
        catch (ServerException e){
            log.error("Server with serverID {} not found",form.getServerId());
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY, String.format("Server with ID %s not found",form.getServerId()));
        }
        Optional<GoogleConferenceEntity> optionalGoogleConferenceEntity = googleConferenceRepository.findByOrganizerEmailAndEventId(email,eventId);
        GoogleConferenceEntity conferenceEntity = optionalGoogleConferenceEntity.orElseGet(()->{
            GoogleConferenceEntity newEntity = googleConferenceFormToGoogleConferenceEntityReducer.reduce(form,userVo);
            return googleConferenceRepository.save(newEntity);
        });
        GoogleConferenceDataVo googleConferenceDataVo = googleConferenceEntityToGoogleConferenceDataVoReducer.reduce(conferenceEntity,email, serverVo.getServerUrl());
        return googleConferenceDataVo;
    }

    @Override
    public GoogleConferenceVo getGoogleConferenceByUserEmailAndEventId(String email, String eventId) throws GoogleConferenceException {
        GoogleConferenceVo conferenceVo = null;
        UserVo userVo;
        try{
            userVo = userService.getUserByEmail(email);
        }
        catch (UserException e) {
            log.error("User with calendarId/email {} not found",email);
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,String.format("User with calendarId/email %s not found",email));
        }
        Optional<GoogleConferenceEntity> optionalConferenceEntity = googleConferenceRepository.findByOrganizerEmailAndEventId(email,eventId);
        if(optionalConferenceEntity.isEmpty())
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,String.format("Calendar event with ID %s not found",eventId));
        else{
            conferenceVo = googleConferenceEntityToGoogleConferenceVoConverter.convert(optionalConferenceEntity.get());
        }
        return conferenceVo;
    }

    @Override
    public List<GoogleConferenceVo> getAllGoogleConferencesByUserEmail(String email) throws GoogleConferenceException {
        UserVo userVo;
        try{
            userVo = userService.getUserByEmail(email);
        }
        catch (UserException e) {
            log.error("User with calendarId/email {} not found",email);
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY, String.format("User with calendarId/email %s not found",email));
        }
        List<GoogleConferenceVo> conferenceVoList = googleConferenceRepository.findAllByOrganizerEmail(email)
                .stream()
                .map(e-> googleConferenceEntityToGoogleConferenceVoConverter.convert(e))
                .collect(Collectors.toList());
        return conferenceVoList;
    }

    @Override
    public void patchGoogleConferenceByUserEmailAndEventId(String email, String eventId, JsonPatch patches) throws GoogleConferenceException {
        UserVo userVo;
        try{
            userVo = userService.getUserByEmail(email);
        }
        catch (UserException e) {
            log.error("User with calendarId/email {} not found",email);
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY, String.format("User with calendarId/email %s not found",email));
        }
        Optional<GoogleConferenceEntity> optionalConferenceEntity = googleConferenceRepository.findByOrganizerEmailAndEventId(email,eventId);
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
     * A patch may only move a conference through its lifecycle. Rewriting the primary key, the
     * calendar event it belongs to, the room people join or the organizer who owns it would either
     * orphan a live meeting or hand the conference to another user, so those are refused.
     */
    private void rejectIdentityChanges(GoogleConferenceEntity original, GoogleConferenceEntity patched) throws GoogleConferenceException {
        if(Objects.equals(original.getId(), patched.getId())
                && Objects.equals(original.getEventId(), patched.getEventId())
                && Objects.equals(original.getRoomId(), patched.getRoomId())
                && Objects.equals(original.getOrganizerEmail(), patched.getOrganizerEmail())){
            return;
        }
        log.error("Patch attempted to change immutable attributes of the conference with event id {}", original.getEventId());
        throw new GoogleConferenceException(GoogleConferenceErrorType.PERMANENT,
                "Only the status of a conference can be patched");
    }

    @Override
    public void deleteGoogleConferenceByUserEmailAndEventId(String email, String eventId) throws GoogleConferenceException {
        UserVo userVo;
        try{
            userVo = userService.getUserByEmail(email);
        }
        catch (UserException e) {
            log.error("User with calendarId/email {} not found",email);
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY, String.format("User with calendarId/email %s not found",email));
        }
        Optional<GoogleConferenceEntity> optionalConferenceEntity = googleConferenceRepository.findByOrganizerEmailAndEventId(email,eventId);
        if(optionalConferenceEntity.isEmpty())
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,String.format("Calendar event with ID %s not found",eventId));
        else googleConferenceRepository.deleteById(optionalConferenceEntity.get().getId());
    }

}
