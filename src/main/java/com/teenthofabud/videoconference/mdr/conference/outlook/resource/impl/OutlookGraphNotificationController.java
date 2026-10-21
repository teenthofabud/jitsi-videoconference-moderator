package com.teenthofabud.videoconference.mdr.conference.outlook.resource.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.teenthofabud.videoconference.mdr.conference.outlook.service.impl.OutlookGraphSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/outlook/graph")
@RequiredArgsConstructor
public class OutlookGraphNotificationController {
    private final OutlookGraphSyncService service;

    @PostMapping(value = "/notifications", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> notifications(@RequestParam(required = false) String validationToken,
                                                @RequestBody(required = false) JsonNode payload) {
        if (validationToken != null) return ResponseEntity.ok(validationToken);
        if (payload != null) service.handleNotifications(payload);
        return ResponseEntity.accepted().body("");
    }

}
