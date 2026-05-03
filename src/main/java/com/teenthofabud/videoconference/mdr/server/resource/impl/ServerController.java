package com.teenthofabud.videoconference.mdr.server.resource.impl;

import com.teenthofabud.videoconference.mdr.server.converter.ServerEntityToVoConverter;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.server.form.ServerPreferenceForm;
import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.form.ServerForm;
import com.teenthofabud.videoconference.mdr.server.resource.ServerAPI;
import com.teenthofabud.videoconference.mdr.server.service.ServerService;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
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
    @Autowired
    ServerEntityToVoConverter serverEntityToVoConverter;

//    @Override
//    @PostMapping
//    // internal endpoint
//    public ResponseEntity<Void> createNewDefaultServer(@Valid @RequestBody ServerForm form) throws ServerException {
//        ServerEntity serverEntity = serverService.createNewDefaultServer(form);
//        ServerVo serverVo = serverEntityToVoConverter.convert(serverEntity);
//        URI location = ServletUriComponentsBuilder
//                .fromCurrentRequest()
//                .path("/{id}")
//                .buildAndExpand(serverVo.getId())
//                .toUri();
//        return ResponseEntity.created(location).build();
//    }

    @Override
    @GetMapping("/{serverId}")
    public ResponseEntity<ServerVo> getServerById(@PathVariable Long serverId) throws ServerException {
        ServerEntity serverEntity = serverService.getServerById(serverId);
        ServerVo serverVo = serverEntityToVoConverter.convert(serverEntity);
        return ResponseEntity.ok(serverVo);
    }

    @Override
    @PostMapping("/preference")
    public ResponseEntity<Void> addServerPreference(@Valid @RequestBody ServerPreferenceForm form) throws UserException, ServerException {
        serverService.addServerPreference(form);
        return ResponseEntity.ok().build();
    }


}
