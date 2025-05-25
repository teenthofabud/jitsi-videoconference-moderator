package com.teenthofabud.videoconference.mdr.server.service;

import com.teenthofabud.videoconference.mdr.server.dto.ServerDto;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.form.ServerForm;

public interface ServerService {

    void createNewServer(ServerForm form) throws ServerException;

    ServerDto getServerByUrl(String serverUrl) throws ServerException;
}
