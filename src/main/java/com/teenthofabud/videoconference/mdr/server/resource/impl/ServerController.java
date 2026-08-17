package com.teenthofabud.videoconference.mdr.server.resource.impl;

import com.teenthofabud.videoconference.mdr.server.form.ServerPreferenceForm;
import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.resource.ServerAPI;
import com.teenthofabud.videoconference.mdr.server.service.ServerService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/server")
@Slf4j
public class ServerController implements ServerAPI {

    @Autowired
    ServerService serverService;

    @Override
    @GetMapping("/{serverId}")
    public ResponseEntity<ServerVo> getServerById(@PathVariable Long serverId) throws ServerException {
        ServerVo serverVo = serverService.getServerById(serverId);
        return ResponseEntity.ok(serverVo);
    }

    @Override
    @PostMapping
    public ResponseEntity<ServerVo> addServerPreference(@Valid @RequestBody ServerPreferenceForm form) throws ServerException {
        ServerVo serverVo = serverService.addServerPreference(form);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(serverVo.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @Override
    @PutMapping
    public ResponseEntity<ServerVo> updateServerPreference(@Valid @RequestBody ServerPreferenceForm form) throws ServerException {
        ServerVo serverVo = serverService.updateServerPreference(form);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(serverVo.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }
}
