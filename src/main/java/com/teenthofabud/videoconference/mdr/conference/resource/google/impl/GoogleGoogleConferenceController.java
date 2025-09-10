package com.teenthofabud.videoconference.mdr.conference.resource.google.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.teenthofabud.videoconference.mdr.conference.vo.ConferenceVo;
import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleCalendarEvent;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.resource.google.GoogleConferenceAPI;
import com.teenthofabud.videoconference.mdr.conference.service.ConferenceService;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceVo;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/event/{eventId}")
    public ResponseEntity<GoogleConferenceVo> getGoogleConferenceByUserAndEventId(@PathVariable String eventId,@RequestParam String userId) throws ConferenceException, GoogleConferenceException {
         GoogleConferenceVo conferenceVo = conferenceService.getGoogleConferenceByUserAndEventId(eventId,userId);
         return ResponseEntity.ok(conferenceVo);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<GoogleConferenceVo>> getAllGoogleConferencesByUser(@RequestParam String userId) throws GoogleConferenceException {
        List<GoogleConferenceVo> conferenceVoList = conferenceService.getAllGoogleConferencesByUserId(userId);
        return ResponseEntity.ok(conferenceVoList);
    }

    @Override
    @PatchMapping("/event/{eventId}")
    public ResponseEntity<Void> patchGoogleConferenceByUserAndEventId(@PathVariable String eventId, @RequestParam String userId, @RequestBody JsonPatch patches) throws GoogleConferenceException, JsonPatchException, JsonProcessingException {
        conferenceService.patchGoogleConferenceByUserAndEventId(eventId,userId,patches);
        return ResponseEntity.ok().build();
    }

    @Override
    @DeleteMapping("/event/{eventId}")
    public ResponseEntity<Void> deleteGoogleConferenceByUserAndEventId(@PathVariable String eventId,@RequestParam String userId) throws GoogleConferenceException {
        conferenceService.deleteGoogleConferenceByUserAndEventId(eventId,userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
