package com.teenthofabud.videoconference.mdr.server.resource.impl;

import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.form.ServerForm;
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
@RequestMapping("/server")
@Slf4j
public class ServerController implements ServerAPI {

    @Autowired
    ServerService serverService;

    @Override
    @PostMapping
    public ResponseEntity<Void> createNewServer(@Valid @RequestBody ServerForm form) throws ServerException {
        ServerVo serverVo = serverService.createNewServer(form);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(serverVo.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @Override
    @GetMapping("/{serverID}")
    public ResponseEntity<ServerVo> getServerByID(@PathVariable Long serverID) throws ServerException {
        ServerVo serverVo = serverService.getServerByID(serverID);
        return ResponseEntity.ok(serverVo);
    }


}
