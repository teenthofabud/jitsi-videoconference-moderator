package com.teenthofabud.videoconference.mdr.conference.outlook.resource.impl;

import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.conference.outlook.form.OutlookAppointmentSentForm;
import com.teenthofabud.videoconference.mdr.conference.outlook.form.OutlookConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.outlook.resource.OutlookConferenceAPI;
import com.teenthofabud.videoconference.mdr.conference.outlook.service.OutlookConferenceService;
import com.teenthofabud.videoconference.mdr.conference.outlook.vo.OutlookConferenceVo;
import com.teenthofabud.videoconference.mdr.conference.outlook.vo.OutlookConferenceSetupVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/outlook/conferences")
@RequiredArgsConstructor
public class OutlookConferenceController implements OutlookConferenceAPI {

    private final OutlookConferenceService service;

    @Override
    @GetMapping("/setup")
    public ResponseEntity<OutlookConferenceSetupVo> getSetup(@RequestParam String email) throws ConferenceException {
        return ResponseEntity.ok(service.getSetup(email));
    }

    @Override
    @PostMapping
    public ResponseEntity<OutlookConferenceVo> createConference(@Valid @RequestBody OutlookConferenceForm form)
            throws ConferenceException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createConference(form));
    }

    @Override
    @PostMapping("/{draftTrackingId}/sent")
    public ResponseEntity<OutlookConferenceVo> markAppointmentSent(@PathVariable String draftTrackingId,
            @RequestParam Long userId, @Valid @RequestBody OutlookAppointmentSentForm form)
            throws ConferenceException {
        return ResponseEntity.ok(service.markAppointmentSent(userId, draftTrackingId, form));
    }

    @Override
    @DeleteMapping("/{draftTrackingId}")
    public ResponseEntity<Void> cancelConference(@PathVariable String draftTrackingId,
            @RequestParam Long userId) throws ConferenceException {
        service.cancelConference(userId, draftTrackingId);
        return ResponseEntity.noContent().build();
    }
}
