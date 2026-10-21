package com.teenthofabud.videoconference.mdr.conference.outlook.resource.impl;

import com.teenthofabud.videoconference.mdr.conference.outlook.service.impl.OutlookGraphSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/outlook/oauth")
@RequiredArgsConstructor
public class OutlookGraphOAuthController {
    private final OutlookGraphSyncService service;

    @GetMapping("/connect")
    public ResponseEntity<Void> connect(@RequestParam Long userId) {
        String authorizeUri = service.beginAuthorization(userId);
        return ResponseEntity.status(302).header(HttpHeaders.LOCATION, authorizeUri).build();
    }

    @GetMapping("/admin-consent")
    public ResponseEntity<Void> adminConsent() {
        return ResponseEntity.status(302).header(HttpHeaders.LOCATION, service.buildAdminConsentUrl()).build();
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam(required = false) String code,
                                           @RequestParam(required = false) String state,
                                           @RequestParam(required = false) String error,
                                           @RequestParam(name = "admin_consent", required = false) String adminConsent) {
        String page = adminConsent == null
                ? service.completeAuthorization(code, state, error)
                : service.completeAdminConsent(adminConsent, error);
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(page);
    }

    @GetMapping("/status")
    public Map<String, Object> status(@RequestParam Long userId) {
        return service.getStatus(userId);
    }
}
