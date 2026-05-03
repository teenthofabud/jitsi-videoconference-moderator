package com.teenthofabud.videoconference.mdr.server.service;

import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.form.ServerForm;
import com.teenthofabud.videoconference.mdr.server.form.ServerPreferenceForm;
import com.teenthofabud.videoconference.mdr.user.error.UserException;

import java.util.Optional;

public interface ServerService {

    ServerEntity getServerById(Long serverID) throws ServerException;

    void addServerPreference(ServerPreferenceForm preferences) throws UserException, ServerException;

    void updateServerPreference(ServerPreferenceForm preferences) throws UserException, ServerException;


}
