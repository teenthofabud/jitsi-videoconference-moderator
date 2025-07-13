package com.teenthofabud.videoconference.mdr.serverpreference.resource.impl;

import com.teenthofabud.videoconference.mdr.serverpreference.vo.ServerPreferenceVo;
import com.teenthofabud.videoconference.mdr.serverpreference.error.ServerPreferenceException;
import com.teenthofabud.videoconference.mdr.serverpreference.form.ServerPreferenceForm;
import com.teenthofabud.videoconference.mdr.serverpreference.resource.ServerPreferenceAPI;
import com.teenthofabud.videoconference.mdr.serverpreference.service.ServerPreferenceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/server_preference")
public class ServerPreferenceController implements ServerPreferenceAPI {

    @Autowired
    ServerPreferenceService serverPreferenceService;

    @Override
    @PostMapping
    public ResponseEntity<Void> createServerPreference(@Valid @RequestBody ServerPreferenceForm form) throws ServerPreferenceException {
        serverPreferenceService.createServerPreference(form);
        return ResponseEntity.status(201).build();
    }

}
