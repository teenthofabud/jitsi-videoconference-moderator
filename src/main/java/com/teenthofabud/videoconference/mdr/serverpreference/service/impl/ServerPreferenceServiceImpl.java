package com.teenthofabud.videoconference.mdr.serverpreference.service.impl;

import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.serverpreference.converter.ServerPreferenceEntityToDtoConverter;
import com.teenthofabud.videoconference.mdr.serverpreference.dto.ServerPreferenceDto;
import com.teenthofabud.videoconference.mdr.serverpreference.entity.ServerPreferenceEntity;
import com.teenthofabud.videoconference.mdr.serverpreference.error.ServerPreferenceException;
import com.teenthofabud.videoconference.mdr.serverpreference.repository.ServerPreferenceRepository;
import com.teenthofabud.videoconference.mdr.serverpreference.service.ServerPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServerPreferenceServiceImpl implements ServerPreferenceService {

    @Autowired
    ServerPreferenceRepository serverPreferenceRepository;
    @Autowired
    ServerPreferenceEntityToDtoConverter serverPreferenceEntityToDtoConverter;
    @Override
    public ServerPreferenceDto getServerPreferenceByUserId(String userId) throws ServerPreferenceException {
        ServerPreferenceEntity serverPreferenceEntity = serverPreferenceRepository.findByUserId(userId);
        if(serverPreferenceEntity==null)
            throw new ServerPreferenceException(MDRErrorCode.MDR_NOT_FOUND,new Object[]{userId});
        return serverPreferenceEntityToDtoConverter.convert(serverPreferenceEntity);
    }
}
