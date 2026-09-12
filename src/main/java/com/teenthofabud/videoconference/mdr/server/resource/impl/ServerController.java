package com.teenthofabud.videoconference.mdr.server.resource.impl;

import com.teenthofabud.videoconference.mdr.server.form.DefaultServerForm;
import com.teenthofabud.videoconference.mdr.server.form.DefaultServerUpdateForm;
import com.teenthofabud.videoconference.mdr.server.form.ServerPreferenceForm;
import com.teenthofabud.videoconference.mdr.server.form.ServerPreferenceUpdateForm;
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
import java.util.List;

@RestController
@RequestMapping("/api/servers")
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
        return ResponseEntity.created(location).body(serverVo);
    }

    @Override
    @PostMapping("/default")
    public ResponseEntity<ServerVo> createDefaultServer(@Valid @RequestBody DefaultServerForm form) throws ServerException {
        ServerVo serverVo = serverService.createDefaultServer(form);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(serverVo.getId())
                .toUri();
        return ResponseEntity.created(location).body(serverVo);
    }

    @Override
    @PutMapping("/default")
    public ResponseEntity<ServerVo> updateDefaultServer(DefaultServerUpdateForm form) throws ServerException {
        ServerVo serverVo = serverService.updateDefaultServer(form);
        return ResponseEntity.ok(serverVo);
    }

    @Override
    @PutMapping
    public ResponseEntity<ServerVo> updateServerPreference(@Valid @RequestBody ServerPreferenceUpdateForm form) throws ServerException {
        ServerVo serverVo = serverService.updateServerPreference(form);
        return ResponseEntity.ok(serverVo);
    }

    @Override
    @GetMapping
    // guard against IDOR by changing PK to UUIDv7 or alternatives or strong OAuth security later
    public ResponseEntity<List<ServerVo>> getServersByUserID(@RequestParam Long userId) throws ServerException {
        return ResponseEntity.ok(serverService.getServersByUserId(userId));
    }
}
