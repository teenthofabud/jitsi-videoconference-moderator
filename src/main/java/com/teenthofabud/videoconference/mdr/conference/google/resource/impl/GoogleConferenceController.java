package com.teenthofabud.videoconference.mdr.conference.google.resource.impl;

import jakarta.json.JsonPatch;
import com.teenthofabud.videoconference.mdr.conference.google.error.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.google.form.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.google.vo.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.google.resource.GoogleConferenceAPI;
import com.teenthofabud.videoconference.mdr.conference.google.service.GoogleConferenceService;
import com.teenthofabud.videoconference.mdr.conference.google.vo.GoogleConferenceVo;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/google/conferences")
public class GoogleConferenceController implements GoogleConferenceAPI {

    @Autowired
    GoogleConferenceService googleConferenceService;

    @Override
    @PostMapping
    public ResponseEntity<GoogleConferenceDataVo> createNewGoogleConference(@Valid @RequestBody GoogleConferenceForm form) throws GoogleConferenceException {
        log.debug("Received request with body : {}",form);
        GoogleConferenceDataVo googleConferenceDataVo = googleConferenceService.createNewGoogleConference(form);
        return ResponseEntity.status(HttpStatus.CREATED).body(googleConferenceDataVo);
    }

    @Override
    @GetMapping("/events/{eventId}")
    public ResponseEntity<GoogleConferenceVo> getGoogleConferenceByUserIdAndEventId(@PathVariable String eventId, @RequestParam Long userId) throws GoogleConferenceException {
         GoogleConferenceVo conferenceVo = googleConferenceService.getGoogleConferenceByUserIdAndEventId(userId,eventId);
         return ResponseEntity.ok(conferenceVo);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<GoogleConferenceVo>> getAllGoogleConferencesByUserId(@RequestParam Long userId) throws GoogleConferenceException {
        List<GoogleConferenceVo> conferenceVoList = googleConferenceService.getAllGoogleConferencesByUserId(userId);
        return ResponseEntity.ok(conferenceVoList);
    }

    @Override
    @PatchMapping("/events/{eventId}")
    public ResponseEntity<Void> patchGoogleConferenceByUserIdAndEventId(@PathVariable String eventId, @RequestParam Long userId, @RequestBody JsonPatch patches) throws GoogleConferenceException {
        googleConferenceService.patchGoogleConferenceByUserIdAndEventId(userId,eventId,patches);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Void> deleteGoogleConferenceByUserIdAndEventId(@PathVariable String eventId, @RequestParam Long userId) throws GoogleConferenceException {
        googleConferenceService.deleteGoogleConferenceByUserIdAndEventId(userId,eventId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
