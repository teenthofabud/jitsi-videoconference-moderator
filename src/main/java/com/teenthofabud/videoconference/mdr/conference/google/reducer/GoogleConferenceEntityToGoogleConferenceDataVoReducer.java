package com.teenthofabud.videoconference.mdr.conference.google.reducer;

import com.teenthofabud.videoconference.mdr.conference.google.entity.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.google.vo.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.vo.UserVo;
import org.springframework.stereotype.Component;

@Component
public class GoogleConferenceEntityToGoogleConferenceDataVoReducer {

    public GoogleConferenceDataVo reduce(GoogleConferenceEntity conferenceEntity,Long userId,String serverUrl){

            return GoogleConferenceDataVo.builder()
                    .userId(userId)
                    .eventId(conferenceEntity.getEventId())
                    .videoUri(String.format("%s/%s",withScheme(serverUrl), conferenceEntity.getRoomId()))
                    .id(conferenceEntity.getRoomId())
                    .serverId(conferenceEntity.getServerId())
                    .build();
    }

    // server URLs are sanitised to carry https:// before they are persisted; rows written before
    // that was the case may still be bare hosts
    private String withScheme(String serverUrl){
        if(serverUrl == null || serverUrl.startsWith("https://") || serverUrl.startsWith("http://")){
            return serverUrl;
        }
        return "https://" + serverUrl;
    }
}
