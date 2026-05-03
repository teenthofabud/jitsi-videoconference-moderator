package com.teenthofabud.videoconference.mdr.conference.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
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
import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.service.ServerService;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GoogleConferenceServiceImpl implements GoogleConferenceService {

    @Autowired
    GoogleConferenceRepository googleConferenceRepository;
    @Autowired
    ConferenceEntityToConferenceVoConverter conferenceEntityToConferenceVoConverter;
    @Autowired
    GoogleConferenceEntityToGoogleConferenceVoConverter googleConferenceEntityToGoogleConferenceVoConverter;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    GoogleConferenceFormToGoogleConferenceEntityReducer googleConferenceFormToGoogleConferenceEntityReducer;
    @Autowired
    GoogleConferenceEntityToGoogleConferenceDataVoReducer googleConferenceEntityToGoogleConferenceDataVoReducer;
    @Autowired
    UserService userService;

    @Override
    public GoogleConferenceDataVo createNewGoogleConference(GoogleConferenceForm form) throws GoogleConferenceException {
        String email = form.getEventData().getCalendarId();
        String eventId = form.getEventData().getEventId();
        UserEntity userEntity;
        try{
            userEntity = userService.getUserByEmail(email);
        }
        catch (UserException e) {
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY, MDRErrorCode.MDR_ATTRIBUTE_INVALID,new Object[]{email});
        }
        if(userEntity.getPreferredServer()==null)
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY, MDRErrorCode.MDR_ATTRIBUTE_MISSING,new Object[]{"Preferred Server",email});
        Optional<GoogleConferenceEntity> optionalGoogleConferenceEntity = googleConferenceRepository.findByOrganizerEmailAndEventId(userEntity.getEmail(),eventId);
        GoogleConferenceEntity conferenceEntity = optionalGoogleConferenceEntity.orElseGet(()->{
            GoogleConferenceEntity newEntity = googleConferenceFormToGoogleConferenceEntityReducer.reduce(form,userEntity);
            return googleConferenceRepository.save(newEntity);
        });
        GoogleConferenceDataVo googleConferenceDataVo = googleConferenceEntityToGoogleConferenceDataVoReducer.reduce(conferenceEntity,userEntity);
        return googleConferenceDataVo;
    }

    @Override
    public GoogleConferenceVo getGoogleConferenceByUserEmailAndEventId(String email, String eventId) throws GoogleConferenceException, UserException {
        GoogleConferenceVo conferenceVo = null;
        UserEntity userEntity = userService.getUserByEmail(email);
        Optional<GoogleConferenceEntity> optionalConferenceEntity = googleConferenceRepository.findByOrganizerEmailAndEventId(userEntity.getEmail(),eventId);
        if(optionalConferenceEntity.isEmpty())
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,MDRErrorCode.MDR_NOT_FOUND,new Object[]{eventId});
        else{
            conferenceVo = googleConferenceEntityToGoogleConferenceVoConverter.convert(optionalConferenceEntity.get());
        }
        return conferenceVo;
    }

    @Override
    public List<GoogleConferenceVo> getAllGoogleConferencesByUserEmail(String email) throws GoogleConferenceException, UserException {
        UserEntity userEntity = userService.getUserByEmail(email);
        List<GoogleConferenceVo> conferenceVoList = googleConferenceRepository.findAllByOrganizerEmail(userEntity.getEmail())
                .stream()
                .map(e-> googleConferenceEntityToGoogleConferenceVoConverter.convert(e))
                .collect(Collectors.toList());
        return conferenceVoList;
    }

    @Override
    public void patchGoogleConferenceByUserEmailAndEventId(String email, String eventId, JsonPatch patches) throws GoogleConferenceException, JsonPatchException, JsonProcessingException, UserException {
        UserEntity userEntity = userService.getUserByEmail(email);
        Optional<GoogleConferenceEntity> optionalConferenceEntity = googleConferenceRepository.findByOrganizerEmailAndEventId(userEntity.getEmail(),eventId);
        if(optionalConferenceEntity.isEmpty())
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,MDRErrorCode.MDR_NOT_FOUND,new Object[]{eventId});
        GoogleConferenceEntity conferenceEntity = optionalConferenceEntity.get();
        JsonNode jsonNode = objectMapper.valueToTree(conferenceEntity);
        log.info("Before patch : {}",objectMapper.writeValueAsString(conferenceEntity));
        JsonNode patchedNode = patches.apply(jsonNode);
        conferenceEntity = objectMapper.treeToValue(patchedNode,GoogleConferenceEntity.class);
        log.info("after patching : {}",conferenceEntity);
        googleConferenceRepository.save(conferenceEntity);
    }

    @Override
    public void deleteGoogleConferenceByUserEmailAndEventId(String email, String eventId) throws GoogleConferenceException, UserException {
        UserEntity userEntity = userService.getUserByEmail(email);
        Optional<GoogleConferenceEntity> optionalConferenceEntity = googleConferenceRepository.findByOrganizerEmailAndEventId(userEntity.getEmail(),eventId);
        if(optionalConferenceEntity.isEmpty())
            throw new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,MDRErrorCode.MDR_NOT_FOUND,new Object[]{email});
        else googleConferenceRepository.deleteById(optionalConferenceEntity.get().getId());
    }

}
