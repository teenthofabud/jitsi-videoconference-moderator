package com.teenthofabud.videoconference.mdr.conference.reducer;

import com.teenthofabud.videoconference.mdr.conference.entity.google.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.serverpreference.vo.ServerPreferenceVo;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import com.teenthofabud.videoconference.mdr.user.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoogleConferenceEntityToGoogleConferenceDataVoReducer {

    @Autowired
    UserService userService;

    public GoogleConferenceDataVo reduce(GoogleConferenceEntity conferenceEntity, String serverUrl, String userId){

            return GoogleConferenceDataVo.builder()
                    .adminEmail(userId)
                    .eventId(conferenceEntity.getEventId())
                    .videoUri(String.format("https://%s/%s",serverUrl, conferenceEntity.getRoomId()))
                    .id(conferenceEntity.getRoomId())
                    .build();
    }
}
