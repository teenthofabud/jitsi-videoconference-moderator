package com.teenthofabud.videoconference.mdr.conference.resource.google.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceFormV2;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.resource.google.GoogleConferenceAPI;
import com.teenthofabud.videoconference.mdr.conference.service.GoogleConferenceService;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceVo;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/google/conference")
public class GoogleGoogleConferenceController implements GoogleConferenceAPI {

    @Autowired
    GoogleConferenceService googleConferenceService;

    @Override
    @PostMapping
    public ResponseEntity<GoogleConferenceDataVo> createNewGoogleConference(@Valid @RequestBody GoogleConferenceFormV2 form) throws GoogleConferenceException {
        log.debug("Received request with body : {}",form);
        GoogleConferenceDataVo googleConferenceDataVo = googleConferenceService.createNewGoogleConference(form);
        return ResponseEntity.status(HttpStatus.CREATED).body(googleConferenceDataVo);
    }

    @Override
    @GetMapping("/event/{eventId}")
    public ResponseEntity<GoogleConferenceVo> getGoogleConferenceByUserEmailAndEventId(@PathVariable String eventId, @RequestParam String email) throws ConferenceException, GoogleConferenceException, UserException {
         GoogleConferenceVo conferenceVo = googleConferenceService.getGoogleConferenceByUserEmailAndEventId(email,eventId);
         return ResponseEntity.ok(conferenceVo);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<GoogleConferenceVo>> getAllGoogleConferencesByUserEmail(@RequestParam String email) throws GoogleConferenceException, UserException {
        List<GoogleConferenceVo> conferenceVoList = googleConferenceService.getAllGoogleConferencesByUserEmail(email);
        return ResponseEntity.ok(conferenceVoList);
    }

    @Override
    @PatchMapping("/event/{eventId}")
    public ResponseEntity<Void> patchGoogleConferenceByUserEmailAndEventId(@PathVariable String eventId, @RequestParam String email, @RequestBody JsonPatch patches) throws GoogleConferenceException, JsonPatchException, JsonProcessingException, UserException {
        googleConferenceService.patchGoogleConferenceByUserEmailAndEventId(email,eventId,patches);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @DeleteMapping("/event/{eventId}")
    public ResponseEntity<Void> deleteGoogleConferenceByUserEmailAndEventId(@PathVariable String email,@RequestParam String eventId) throws GoogleConferenceException, UserException {
        googleConferenceService.deleteGoogleConferenceByUserEmailAndEventId(email,eventId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
