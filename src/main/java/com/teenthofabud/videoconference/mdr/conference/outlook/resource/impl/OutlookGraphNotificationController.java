package com.teenthofabud.videoconference.mdr.conference.outlook.resource.impl;

import com.teenthofabud.videoconference.mdr.conference.outlook.dto.OutlookGraphDtos.NotificationPayload;
import com.teenthofabud.videoconference.mdr.conference.outlook.service.impl.OutlookGraphSyncService;
import tools.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/notifications", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> notifications(@RequestParam(required = false) String validationToken,
                                                @RequestBody(required = false) String payloadBody) throws Exception {
        if (validationToken != null) return ResponseEntity.ok(validationToken);
        if (payloadBody != null && !payloadBody.isBlank()) {
            service.handleNotifications(objectMapper.readValue(payloadBody, NotificationPayload.class));
        }
        return ResponseEntity.accepted().body("");
    }

}
