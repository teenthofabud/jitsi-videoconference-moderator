package com.teenthofabud.videoconference.mdr.conference.google.reducer;

import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import com.teenthofabud.videoconference.mdr.conference.google.entity.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.google.form.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.user.vo.UserVo;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GoogleConferenceFormToGoogleConferenceEntityReducer {

    public GoogleConferenceEntity reduce(GoogleConferenceForm form, UserVo user){
        String conferenceId = UUID.randomUUID().toString();
        return GoogleConferenceEntity.builder()
                .eventId(form.getEventId())
                .status(ConferenceStatus.PENDING)
                .serverId(form.getServerId())
                .userId(user.getId())
                .roomId(conferenceId)
                .build();
    }
}
