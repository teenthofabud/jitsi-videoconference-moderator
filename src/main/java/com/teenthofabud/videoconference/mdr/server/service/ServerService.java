package com.teenthofabud.videoconference.mdr.server.service;

import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.form.ServerForm;
import com.teenthofabud.videoconference.mdr.user.error.UserException;

public interface ServerService {

    ServerEntity createNewCustomServer(ServerForm form) throws ServerException;

    ServerEntity createNewDefaultServer(ServerForm form) throws ServerException;

    ServerEntity getServerById(Long serverID) throws ServerException;

    ServerEntity getServerByUrl(String url) throws ServerException;

}
