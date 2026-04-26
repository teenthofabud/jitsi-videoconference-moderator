package com.teenthofabud.videoconference.mdr.conference.reducer;

import com.teenthofabud.videoconference.mdr.conference.entity.google.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import org.springframework.stereotype.Component;

@Component
public class GoogleConferenceEntityToGoogleConferenceDataVoReducer {

    public GoogleConferenceDataVo reduce(GoogleConferenceEntity conferenceEntity, String serverUrl, String email){

            return GoogleConferenceDataVo.builder()
                    .adminEmail(email)
                    .eventId(conferenceEntity.getEventId())
                    .videoUri(String.format("https://%s/%s",serverUrl, conferenceEntity.getRoomId()))
                    .id(conferenceEntity.getRoomId())
                    .build();
    }
}
