package com.teenthofabud.videoconference.moderator.conference.resource.Impl;

import com.teenthofabud.videoconference.moderator.conference.dto.ConferenceDto;
import com.teenthofabud.videoconference.moderator.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.moderator.conference.resource.ConferenceAPI;
import com.teenthofabud.videoconference.moderator.conference.service.ConferenceService;
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
    @PostMapping
    public ResponseEntity<ConferenceDto> createNewGoogleConference(@RequestBody GoogleConferenceForm form) {
        log.info("Received request with body : {}",form);
        ConferenceDto conferenceDto = null;
        conferenceDto = conferenceService.createNewGoogleConference(form);
        return ResponseEntity.ok(conferenceDto);
    }
}
