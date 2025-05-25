package com.teenthofabud.videoconference.mdr.conference.resource.impl;

import com.github.fge.jsonpatch.JsonPatch;
import com.teenthofabud.videoconference.mdr.conference.dto.ConferenceDto;
import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleCalendarEvent;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.resource.ConferenceAPI;
import com.teenthofabud.videoconference.mdr.conference.service.ConferenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/conference")
public class ConferenceController implements ConferenceAPI {

    @Autowired
    ConferenceService conferenceService;

    @Override
    @PostMapping("/google")
    public ResponseEntity<GoogleConferenceDataVo> createNewGoogleConference(@RequestBody GoogleConferenceForm form) throws GoogleConferenceException {
        log.info("Received request with body : {}",form);
        GoogleConferenceDataVo googleConferenceDataVo = null;
        googleConferenceDataVo = conferenceService.createNewGoogleConference(form);
        return ResponseEntity.status(HttpStatus.CREATED).body(googleConferenceDataVo);
    }

    @Override
    public ResponseEntity<ConferenceDto> getConferenceByID(Long conferenceId) throws ConferenceException {
         ConferenceDto conferenceDto = conferenceService.getConferenceById(conferenceId);
         return ResponseEntity.ok(conferenceDto);
    }

    @Override
    public ResponseEntity<List<ConferenceDto>> getAllConferencesByUser(String calendarId) {
        List<ConferenceDto> conferenceDtoList = conferenceService.getAllConferencesByUserId(calendarId);
        return ResponseEntity.ok(conferenceDtoList);
    }

    @Override
    public ResponseEntity<Void> patchGoogleConferenceByID(Long conferenceId, GoogleCalendarEvent form) {
        return null;
    }

    @Override
    public ResponseEntity<Void> patchConferenceByID(Long conferenceId, JsonPatch jsonPatch) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteConferenceByID(Long conferenceId) throws ConferenceException {
        conferenceService.deleteConferenceById(conferenceId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
