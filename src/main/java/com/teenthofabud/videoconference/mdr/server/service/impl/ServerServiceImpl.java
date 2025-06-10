package com.teenthofabud.videoconference.mdr.server.service.impl;

import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.server.converter.ServerEntityToDtoConverter;
import com.teenthofabud.videoconference.mdr.server.converter.ServerFormToEntityConverter;
import com.teenthofabud.videoconference.mdr.server.dto.ServerDto;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.form.ServerForm;
import com.teenthofabud.videoconference.mdr.server.repository.ServerRepository;
import com.teenthofabud.videoconference.mdr.server.service.ServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ServerServiceImpl implements ServerService {

    @Autowired
    ServerRepository serverRepository;
    @Autowired
    ServerEntityToDtoConverter serverEntityToDtoConverter;
    @Autowired
    ServerFormToEntityConverter serverFormToEntityConverter;

    @Override
    public void createNewServer(ServerForm form) throws ServerException {
        Optional<ServerEntity> existingServer = serverRepository.findByServerUrl(form.getServerUrl());
        if(existingServer.isPresent())
            throw new ServerException(MDRErrorCode.MDR_EXISTS,new Object[]{form.getServerUrl()});
        ServerEntity server = serverFormToEntityConverter.convert(form);
        serverRepository.save(server);
    }

    @Override
    public ServerDto getServerByUrl(String serverUrl) throws ServerException {
        return serverRepository.findByServerUrl(serverUrl).map(e->serverEntityToDtoConverter.convert(e)).orElseThrow(() -> new ServerException(MDRErrorCode.MDR_NOT_FOUND,new Object[]{serverUrl}));
    }
}
