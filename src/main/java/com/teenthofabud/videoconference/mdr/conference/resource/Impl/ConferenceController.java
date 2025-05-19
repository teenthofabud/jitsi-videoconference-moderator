package com.teenthofabud.videoconference.mdr.conference.resource.Impl;

import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.error.google.UserNotFoundException;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.resource.ConferenceAPI;
import com.teenthofabud.videoconference.mdr.conference.service.ConferenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/conference")
public class ConferenceController implements ConferenceAPI {

    @Autowired
    ConferenceService conferenceService;

    @Override
    @PostMapping("/google")
    public ResponseEntity<GoogleConferenceDataVo> createNewGoogleConference(@RequestBody GoogleConferenceForm form) throws UserNotFoundException {
        log.info("Received request with body : {}",form);
        GoogleConferenceDataVo googleConferenceDataVo = null;
        googleConferenceDataVo = conferenceService.createNewGoogleConference(form);
        return ResponseEntity.ok(googleConferenceDataVo);
    }
}
