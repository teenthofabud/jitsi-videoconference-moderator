package com.teenthofabud.videoconference.mdr.conference.reducer;

import com.teenthofabud.videoconference.mdr.conference.entity.google.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.vo.UserVo;
import org.springframework.stereotype.Component;

@Component
public class GoogleConferenceEntityToGoogleConferenceDataVoReducer {

    public GoogleConferenceDataVo reduce(GoogleConferenceEntity conferenceEntity,Long userId,String serverUrl){

            return GoogleConferenceDataVo.builder()
                    .userId(userId)
                    .eventId(conferenceEntity.getEventId())
                    .videoUri(String.format("%s/%s",serverUrl, conferenceEntity.getRoomId()))
                    .id(conferenceEntity.getRoomId())
                    .build();
    }
}
