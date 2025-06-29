package com.teenthofabud.videoconference.mdr.server.service.impl;

import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.server.converter.ServerEntityToVoConverter;
import com.teenthofabud.videoconference.mdr.server.converter.ServerFormToEntityConverter;
import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;
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
    ServerEntityToVoConverter serverEntityToVoConverter;
    @Autowired
    ServerFormToEntityConverter serverFormToEntityConverter;

    @Override
    public ServerVo createNewServer(ServerForm form) throws ServerException {
        Optional<ServerEntity> existingServer = serverRepository.findByServerUrl(form.getServerUrl());
        if(existingServer.isPresent())
            throw new ServerException(MDRErrorCode.MDR_EXISTS,new Object[]{form.getServerUrl()});
        ServerEntity server = serverFormToEntityConverter.convert(form);
        server = serverRepository.save(server);
        return serverEntityToVoConverter.convert(server);
    }

    @Override
    public ServerVo getServerByID(Long id) throws ServerException {
        return serverRepository.findById(id).map(e-> serverEntityToVoConverter.convert(e)).orElseThrow(() -> new ServerException(MDRErrorCode.MDR_NOT_FOUND,new Object[]{id}));
    }

}
