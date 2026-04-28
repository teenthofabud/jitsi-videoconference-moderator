package com.teenthofabud.videoconference.mdr.conference.reducer;

import com.teenthofabud.videoconference.mdr.conference.entity.google.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class GoogleConferenceEntityToGoogleConferenceDataVoReducer {

    public GoogleConferenceDataVo reduce(GoogleConferenceEntity conferenceEntity, UserEntity userEntity){

            return GoogleConferenceDataVo.builder()
                    .adminEmail(userEntity.getEmail())
                    .eventId(conferenceEntity.getEventId())
                    .videoUri(String.format("https://%s/%s",userEntity.getPreferredServer().getServerUrl(), conferenceEntity.getRoomId()))
                    .id(conferenceEntity.getRoomId())
                    .build();
    }
}
