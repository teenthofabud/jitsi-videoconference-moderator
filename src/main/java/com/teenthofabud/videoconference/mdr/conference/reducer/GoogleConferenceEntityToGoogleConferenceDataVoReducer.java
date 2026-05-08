package com.teenthofabud.videoconference.mdr.conference.reducer;

import com.teenthofabud.videoconference.mdr.conference.entity.google.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.vo.UserVo;
import org.springframework.stereotype.Component;

@Component
public class GoogleConferenceEntityToGoogleConferenceDataVoReducer {

    public GoogleConferenceDataVo reduce(GoogleConferenceEntity conferenceEntity, UserVo userVo){

            return GoogleConferenceDataVo.builder()
                    .adminEmail(userVo.getEmail())
                    .eventId(conferenceEntity.getEventId())
                    .videoUri(String.format("https://%s/%s",userVo.getPreferredServerUrl(), conferenceEntity.getRoomId()))
                    .id(conferenceEntity.getRoomId())
                    .build();
    }
}
