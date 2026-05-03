package com.teenthofabud.videoconference.mdr.server.service.impl;

import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.server.constants.ServerType;
import com.teenthofabud.videoconference.mdr.server.converter.ServerFormToEntityConverter;
import com.teenthofabud.videoconference.mdr.server.form.ServerPreferenceForm;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.repository.ServerRepository;
import com.teenthofabud.videoconference.mdr.server.service.ServerService;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ServerServiceImpl implements ServerService {

    @Autowired
    ServerRepository serverRepository;
    @Autowired
    ServerFormToEntityConverter serverFormToEntityConverter;
    @Autowired
    UserRepository userRepository;


    @Override
    public ServerEntity getServerById(Long id) throws ServerException {
        return serverRepository.findById(id)
                .orElseThrow(() -> new ServerException(MDRErrorCode.MDR_NOT_FOUND,new Object[]{id}));
    }


    @Override
    @Transactional
    public void addServerPreference(ServerPreferenceForm form) throws UserException, ServerException {
        UserEntity user = userRepository.findByEmail(form.getEmail())
                .orElseThrow(() -> new UserException(MDRErrorCode.MDR_NOT_FOUND,
                        new Object[]{form.getEmail()}));
        Optional<ServerEntity> existingServer = serverRepository.findByOwner(user);
        if(existingServer.isPresent())
            throw new ServerException(MDRErrorCode.MDR_EXISTS,new Object[]{form.getPreferredServerUrl(),user.getEmail()});
        ServerEntity server = ServerEntity.builder()
                .serverType(ServerType.CUSTOM)
                .owner(user)
                .serverUrl(form.getPreferredServerUrl())
                .name(form.getName())
                .build();
        server = serverRepository.save(server);
        user.setPreferredServer(server);
    }

    @Override
    @Transactional
    public void updateServerPreference(ServerPreferenceForm form) throws UserException, ServerException {
        UserEntity user = userRepository.findByEmail(form.getEmail())
                .orElseThrow(() -> new UserException(MDRErrorCode.MDR_NOT_FOUND,
                        new Object[]{form.getEmail()}));
        Optional<ServerEntity> optionalServerEntity = serverRepository.findByOwner(user);
        if(optionalServerEntity.isEmpty())
            throw new ServerException(MDRErrorCode.MDR_NOT_FOUND,new Object[]{user.getEmail()});
        ServerEntity existingServer = optionalServerEntity.get();
        existingServer.setServerUrl(form.getPreferredServerUrl());
        existingServer.setName(form.getName());
        existingServer = serverRepository.save(existingServer);
        user.setPreferredServer(existingServer);
    }

}
