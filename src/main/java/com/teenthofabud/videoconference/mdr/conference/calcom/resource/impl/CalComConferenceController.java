package com.teenthofabud.videoconference.mdr.conference.calcom.resource.impl;

import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.conference.calcom.form.CalComConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.calcom.resource.CalComConferenceAPI;
import com.teenthofabud.videoconference.mdr.conference.calcom.service.CalComConferenceService;
import com.teenthofabud.videoconference.mdr.conference.calcom.vo.CalComConferenceVo;
import jakarta.json.JsonPatch;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/calcom/conferences")
@RequiredArgsConstructor
public class CalComConferenceController implements CalComConferenceAPI {

    private final CalComConferenceService service;

    @Override
    @PostMapping
    public ResponseEntity<CalComConferenceVo> createConference(@Valid @RequestBody CalComConferenceForm form) throws ConferenceException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createConference(form));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<CalComConferenceVo>> getAllConferencesByUserId(@RequestParam Long userId) throws ConferenceException {
        return ResponseEntity.ok(service.getAllConferencesByUserId(userId));
    }

    @Override
    @GetMapping("/{bookingUid}")
    public ResponseEntity<CalComConferenceVo> getConference(@PathVariable String bookingUid,
                                                             @RequestParam Long userId) throws ConferenceException {
        return ResponseEntity.ok(service.getConference(userId, bookingUid));
    }

    @Override
    @DeleteMapping("/{bookingUid}")
    public ResponseEntity<Void> deleteConference(@PathVariable String bookingUid,
                                                  @RequestParam Long userId) throws ConferenceException {
        service.deleteConference(userId, bookingUid);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/{bookingUid}")
    public ResponseEntity<Void> patchConference(@PathVariable String bookingUid,
                                                 @RequestParam Long userId,
                                                 @RequestBody JsonPatch patches) throws ConferenceException {
        service.patchConference(userId, bookingUid, patches);
        return ResponseEntity.noContent().build();
    }
}
