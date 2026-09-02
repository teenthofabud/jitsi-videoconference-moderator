package com.teenthofabud.videoconference.mdr.server.service;

import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.form.ServerPreferenceForm;
import com.teenthofabud.videoconference.mdr.server.form.ServerPreferenceUpdateForm;
import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;

public interface ServerService {

    ServerVo getServerById(Long serverID) throws ServerException;

    ServerVo addServerPreference(ServerPreferenceForm preferences) throws ServerException;

    ServerVo updateServerPreference(ServerPreferenceUpdateForm preferences) throws ServerException;

}
