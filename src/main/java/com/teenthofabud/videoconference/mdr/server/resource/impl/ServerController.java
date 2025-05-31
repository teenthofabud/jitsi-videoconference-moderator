package com.teenthofabud.videoconference.mdr.server.resource.impl;

import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.server.dto.ServerDto;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.form.ServerForm;
import com.teenthofabud.videoconference.mdr.server.resource.ServerAPI;
import com.teenthofabud.videoconference.mdr.server.service.ServerService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/server")
@Slf4j
public class ServerController implements ServerAPI {

    @Autowired
    ServerService serverService;

    @Override
    public ResponseEntity<Void> createNewServer(@Valid ServerForm form) throws ServerException {
        serverService.createNewServer(form);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<ServerDto> getServerByUrl(String serverUrl) throws ServerException {
        ServerDto serverDto = serverService.getServerByUrl(serverUrl);
        return ResponseEntity.ok(serverDto);
    }
}
