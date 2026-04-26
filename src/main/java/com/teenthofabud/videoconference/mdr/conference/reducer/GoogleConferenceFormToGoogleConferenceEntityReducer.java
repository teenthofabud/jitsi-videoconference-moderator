package com.teenthofabud.videoconference.mdr.conference.reducer;

import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import com.teenthofabud.videoconference.mdr.conference.entity.google.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceFormV2;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GoogleConferenceFormToGoogleConferenceEntityReducer {

    public GoogleConferenceEntity reduce(GoogleConferenceFormV2 form, UserEntity user, ServerEntity server){
        String conferenceId = UUID.randomUUID().toString();
        return GoogleConferenceEntity.builder()
                .eventId(form.getEventId())
                .status(ConferenceStatus.PENDING)
                .createdBy(user)
                .server(server)
                .roomId(conferenceId)
                .build();
    }
}
