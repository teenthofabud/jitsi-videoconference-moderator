package com.teenthofabud.videoconference.mdr.server.service.impl;

import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.server.constants.ServerType;
import com.teenthofabud.videoconference.mdr.server.converter.ServerEntityToVoConverter;
import com.teenthofabud.videoconference.mdr.server.converter.ServerFormToEntityConverter;
import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.form.ServerForm;
import com.teenthofabud.videoconference.mdr.server.repository.ServerRepository;
import com.teenthofabud.videoconference.mdr.server.service.ServerService;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ServerServiceImpl implements ServerService {

    @Autowired
    ServerRepository serverRepository;
    @Autowired
    ServerFormToEntityConverter serverFormToEntityConverter;

    @Override
    public ServerEntity createNewCustomServer(ServerForm form) throws ServerException {
        return createNewServer(form,ServerType.CUSTOM);
    }

    @Override
    public ServerEntity createNewDefaultServer(ServerForm form) throws ServerException {
        return createNewServer(form,ServerType.DEFAULT);
    }

    private ServerEntity createNewServer(ServerForm form, ServerType type) throws ServerException {
        Optional<ServerEntity> existingServer = serverRepository.findByServerUrl(form.getServerUrl());
        if(existingServer.isPresent())
            throw new ServerException(MDRErrorCode.MDR_EXISTS,new Object[]{form.getServerUrl()});
        ServerEntity server = serverFormToEntityConverter.convert(form);
        server.setServerType(type);
//        server.setOwner(userEntity);
        server = serverRepository.save(server);
        return server;
    }

    @Override
    public ServerEntity getServerById(Long id) throws ServerException {
        return serverRepository.findById(id)
                .orElseThrow(() -> new ServerException(MDRErrorCode.MDR_NOT_FOUND,new Object[]{id}));
    }

    @Override
    public ServerEntity getServerByUrl(String url) throws ServerException {
        return serverRepository.findByServerUrl(url)
                .orElseThrow(() -> new ServerException(MDRErrorCode.MDR_NOT_FOUND,new Object[]{url}));
    }

}
