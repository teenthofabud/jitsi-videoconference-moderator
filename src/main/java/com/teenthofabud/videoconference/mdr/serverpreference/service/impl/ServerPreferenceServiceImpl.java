package com.teenthofabud.videoconference.mdr.serverpreference.service.impl;

import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.server.repository.ServerRepository;
import com.teenthofabud.videoconference.mdr.serverpreference.converter.ServerPreferenceEntityToVoConverter;
import com.teenthofabud.videoconference.mdr.serverpreference.converter.ServerPreferenceFormToEntityConverter;
import com.teenthofabud.videoconference.mdr.serverpreference.vo.ServerPreferenceVo;
import com.teenthofabud.videoconference.mdr.serverpreference.entity.ServerPreferenceEntity;
import com.teenthofabud.videoconference.mdr.serverpreference.error.ServerPreferenceException;
import com.teenthofabud.videoconference.mdr.serverpreference.form.ServerPreferenceForm;
import com.teenthofabud.videoconference.mdr.serverpreference.repository.ServerPreferenceRepository;
import com.teenthofabud.videoconference.mdr.serverpreference.service.ServerPreferenceService;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class ServerPreferenceServiceImpl implements ServerPreferenceService {

    @Autowired
    ServerPreferenceRepository serverPreferenceRepository;
    @Autowired
    ServerPreferenceEntityToVoConverter serverPreferenceEntityToVoConverter;
    @Autowired
    ServerPreferenceFormToEntityConverter serverPreferenceFormToEntityConverter;
    @Autowired
    ServerRepository serverRepository;


    @Override
    public ServerPreferenceVo getServerPreferenceByUserId(String userId) throws ServerPreferenceException {
        return serverPreferenceRepository.findByUserId(userId).map(e-> serverPreferenceEntityToVoConverter.convert(e)).orElseThrow(() -> new ServerPreferenceException(MDRErrorCode.MDR_NOT_FOUND,new Object[]{userId}));
    }

    @Override
    public ServerPreferenceVo getServerPreferenceById(Long id) throws ServerPreferenceException {
        return serverPreferenceRepository.findById(id).map(e-> serverPreferenceEntityToVoConverter.convert(e)).orElseThrow(() -> new ServerPreferenceException(MDRErrorCode.MDR_NOT_FOUND,new Object[]{id}));
    }

    @Override
    @Transactional
    public void createServerPreference(ServerPreferenceForm form) throws ServerPreferenceException {

        // For now a user can only have on server preference, more might be allowed later
        Optional<ServerPreferenceEntity> optionalServerPreferenceEntity = serverPreferenceRepository.findByUserId(form.getUserId());
        if(optionalServerPreferenceEntity.isPresent())
            throw new ServerPreferenceException(MDRErrorCode.MDR_EXISTS, new Object[]{form.getUserId()});
        ServerEntity serverEntity = serverRepository.findByServerUrl(form.getServerUrl()).orElseThrow(() -> new ServerPreferenceException(MDRErrorCode.MDR_ATTRIBUTE_INVALID, new Object[]{form.getServerUrl()}));
        ServerPreferenceEntity serverPreferenceEntity = serverPreferenceFormToEntityConverter.convert(form);
        serverEntity.addServerPreference(serverPreferenceEntity);
        serverRepository.save(serverEntity);

    }
}
