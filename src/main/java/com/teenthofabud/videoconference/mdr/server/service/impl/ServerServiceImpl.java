package com.teenthofabud.videoconference.mdr.server.service.impl;

import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.server.constants.ServerType;
import com.teenthofabud.videoconference.mdr.server.converter.ServerEntityToVoConverter;
import com.teenthofabud.videoconference.mdr.server.converter.ServerFormToEntityConverter;
import com.teenthofabud.videoconference.mdr.server.form.ServerPreferenceForm;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.repository.ServerRepository;
import com.teenthofabud.videoconference.mdr.server.service.ServerService;
import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ServerServiceImpl implements ServerService {

    @Autowired
    ServerRepository serverRepository;
    @Autowired
    ServerFormToEntityConverter serverFormToEntityConverter;
    @Autowired
    ServerEntityToVoConverter serverEntityToVoConverter;
    @Autowired
    UserRepository userRepository;

    @Override
    public ServerVo getServerById(Long id) throws ServerException {
        return serverRepository.findById(id)
                .map(serverEntityToVoConverter::convert)
                .orElseThrow(() -> {
                    log.error("Server not found with id: {}", id);
                    return new ServerException(
                            MDRErrorCode.MDR_NOT_FOUND,
                            new Object[]{id}
                    );
                });
    }

    @Override
    @Transactional
    public ServerVo addServerPreference(ServerPreferenceForm form) throws ServerException {
        UserEntity user = userRepository.findByEmail(form.getEmail())
                .orElseThrow(() -> {
                    log.error("User doesn't exist with id: {}",form.getEmail());
                    return new ServerException(
                            MDRErrorCode.MDR_ATTRIBUTE_INVALID,
                            new Object[]{form.getEmail()}
                    );
                });
        Optional<ServerEntity> existingServer = serverRepository.findByOwnerEmail(user.getEmail());
        if(existingServer.isPresent()){
            log.error("Preferred server for the user({}) already exists with server url : {}",form.getEmail(),form.getPreferredServerUrl());
            throw new ServerException(MDRErrorCode.MDR_EXISTS,new Object[]{form.getPreferredServerUrl(),user.getEmail()});
        }
        ServerEntity server = ServerEntity.builder()
                .serverType(ServerType.CUSTOM)
                .owner(user)
                .serverUrl(form.getPreferredServerUrl())
                .name(form.getName())
                .build();
        server = serverRepository.save(server);
        user.setPreferredServer(server);
        return serverEntityToVoConverter.convert(server);
    }

    @Override
    @Transactional
    public ServerVo updateServerPreference(ServerPreferenceForm form) throws ServerException {
        UserEntity user = userRepository.findByEmail(form.getEmail())
                .orElseThrow(() -> {
                    log.error("User doesn't exist with id: {}",form.getEmail());
                    return new ServerException(
                            MDRErrorCode.MDR_ATTRIBUTE_INVALID,
                            new Object[]{form.getEmail()}
                    );
                });
        Optional<ServerEntity> optionalServerEntity = serverRepository.findByOwnerEmail(user.getEmail());
        if(optionalServerEntity.isEmpty()) {
            log.error("User({}) doesn't have a preferred server yet",form.getEmail());
            throw new ServerException(MDRErrorCode.MDR_NOT_FOUND, new Object[]{user.getEmail()});
        }
        ServerEntity existingServer = optionalServerEntity.get();
        existingServer.setServerUrl(form.getPreferredServerUrl());
        existingServer.setName(form.getName());
        existingServer = serverRepository.save(existingServer);
        user.setPreferredServer(existingServer);
        return serverEntityToVoConverter.convert(existingServer);
    }
}
