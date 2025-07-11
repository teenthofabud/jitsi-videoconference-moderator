package com.teenthofabud.videoconference.mdr.conference.resource.google.impl;

import com.teenthofabud.videoconference.mdr.conference.vo.ConferenceVo;
import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleCalendarEvent;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.resource.google.GoogleConferenceAPI;
import com.teenthofabud.videoconference.mdr.conference.service.ConferenceService;
import jakarta.validation.Valid;
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
@RequestMapping("/google/conference")
public class GoogleGoogleConferenceController implements GoogleConferenceAPI {

    @Autowired
    ConferenceService conferenceService;

    @Override
    @PostMapping
    public ResponseEntity<GoogleConferenceDataVo> createNewGoogleConference(@Valid @RequestBody GoogleConferenceForm form) throws GoogleConferenceException {
        log.debug("Received request with body : {}",form);
        GoogleConferenceDataVo googleConferenceDataVo = conferenceService.createNewGoogleConference(form);
        return ResponseEntity.status(HttpStatus.CREATED).body(googleConferenceDataVo);
    }

    @Override
    public ResponseEntity<ConferenceVo> getGoogleConferenceByID(Long conferenceId) throws ConferenceException, GoogleConferenceException {
         ConferenceVo conferenceVo = conferenceService.getGoogleConferenceById(conferenceId);
         return ResponseEntity.ok(conferenceVo);
    }

    @Override
    public ResponseEntity<List<ConferenceVo>> getAllGoogleConferencesByUser(String calendarId) throws GoogleConferenceException {
        List<ConferenceVo> conferenceVoList = conferenceService.getAllGoogleConferencesByUserId(calendarId);
        return ResponseEntity.ok(conferenceVoList);
    }

    @Override
    public ResponseEntity<Void> patchGoogleConferenceByID(Long conferenceId, GoogleCalendarEvent form) {
        throw new UnsupportedOperationException("API endpoint not implemented yet");
    }

    @Override
    public ResponseEntity<Void> deleteGoogleConferenceByID(Long conferenceId) throws GoogleConferenceException {
        conferenceService.deleteGoogleConferenceById(conferenceId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
