package com.teenthofabud.videoconference.mdr.server.service;

import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.form.ServerForm;

public interface ServerService {

    ServerVo createNewServer(ServerForm form) throws ServerException;

    ServerVo getServerByID(Long serverID) throws ServerException;

}
