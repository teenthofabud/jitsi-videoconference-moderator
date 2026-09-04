package com.teenthofabud.videoconference.mdr.conference.resource.google.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.resource.google.GoogleConferenceAPI;
import com.teenthofabud.videoconference.mdr.conference.service.GoogleConferenceService;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceVo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/google/conferences")
public class GoogleGoogleConferenceController implements GoogleConferenceAPI {

    @Autowired
    GoogleConferenceService googleConferenceService;

    @Override
    @PostMapping
    public ResponseEntity<GoogleConferenceDataVo> createNewGoogleConference(@RequestBody GoogleConferenceForm form) throws GoogleConferenceException {
        log.debug("Received request with body : {}",form);
        GoogleConferenceDataVo googleConferenceDataVo = googleConferenceService.createNewGoogleConference(form);
        return ResponseEntity.status(HttpStatus.CREATED).body(googleConferenceDataVo);
    }

    @Override
    @GetMapping("/event/{eventId}")
    public ResponseEntity<GoogleConferenceVo> getGoogleConferenceByUserEmailAndEventId(@PathVariable String eventId, @RequestParam String email) throws GoogleConferenceException {
         GoogleConferenceVo conferenceVo = googleConferenceService.getGoogleConferenceByUserEmailAndEventId(email,eventId);
         return ResponseEntity.ok(conferenceVo);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<GoogleConferenceVo>> getAllGoogleConferencesByUserEmail(@RequestParam String email) throws GoogleConferenceException {
        List<GoogleConferenceVo> conferenceVoList = googleConferenceService.getAllGoogleConferencesByUserEmail(email);
        return ResponseEntity.ok(conferenceVoList);
    }

    @Override
    @PatchMapping("/event/{eventId}")
    public ResponseEntity<Void> patchGoogleConferenceByUserEmailAndEventId(@PathVariable String eventId, @RequestParam String email, @RequestBody JsonPatch patches) throws GoogleConferenceException, JsonPatchException, JsonProcessingException {
        googleConferenceService.patchGoogleConferenceByUserEmailAndEventId(email,eventId,patches);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @DeleteMapping("/event/{eventId}")
    public ResponseEntity<Void> deleteGoogleConferenceByUserEmailAndEventId(@PathVariable String email, @RequestParam String eventId) throws GoogleConferenceException {
        googleConferenceService.deleteGoogleConferenceByUserEmailAndEventId(email,eventId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
