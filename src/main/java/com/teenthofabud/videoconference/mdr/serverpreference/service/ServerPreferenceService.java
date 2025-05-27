package com.teenthofabud.videoconference.mdr.serverpreference.service;

import com.teenthofabud.videoconference.mdr.serverpreference.dto.ServerPreferenceDto;
import com.teenthofabud.videoconference.mdr.serverpreference.error.ServerPreferenceException;

public interface ServerPreferenceService {

    ServerPreferenceDto getServerPreferenceByUserId(String userId) throws ServerPreferenceException;
}
