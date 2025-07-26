package com.teenthofabud.videoconference.mdr.serverpreference.service;

import com.teenthofabud.videoconference.mdr.serverpreference.vo.ServerPreferenceVo;
import com.teenthofabud.videoconference.mdr.serverpreference.error.ServerPreferenceException;
import com.teenthofabud.videoconference.mdr.serverpreference.form.ServerPreferenceForm;

public interface ServerPreferenceService {

    ServerPreferenceVo getServerPreferenceByUserIdHash(String userId) throws ServerPreferenceException;

    ServerPreferenceVo getServerPreferenceById(Long id) throws ServerPreferenceException;

    void createServerPreference(ServerPreferenceForm form) throws ServerPreferenceException;
}
