package com.teenthofabud.videoconference.mdr.conference.reducer;

import com.teenthofabud.videoconference.mdr.conference.entity.google.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.serverpreference.vo.ServerPreferenceVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoogleConferenceEntityToGoogleConferenceDataVoReducer {

    public GoogleConferenceDataVo reduce(GoogleConferenceEntity conferenceEntity, ServerPreferenceVo serverPreferenceVo){
        return GoogleConferenceDataVo.builder()
                .adminEmail(serverPreferenceVo.getUserId())
                .eventId(conferenceEntity.getEventId())
                .videoUri(String.format("%s/%s",serverPreferenceVo.getServerUrl(),conferenceEntity.getRoomId()))
                .id(conferenceEntity.getRoomId())
                .build();
    }
}
