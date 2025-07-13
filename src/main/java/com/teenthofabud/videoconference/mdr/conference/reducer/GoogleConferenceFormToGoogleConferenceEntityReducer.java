package com.teenthofabud.videoconference.mdr.conference.reducer;

import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import com.teenthofabud.videoconference.mdr.conference.entity.google.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.serverpreference.vo.ServerPreferenceVo;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GoogleConferenceFormToGoogleConferenceEntityReducer {

    public GoogleConferenceEntity reduce(GoogleConferenceForm form, ServerPreferenceVo serverPreferenceVo){
        String uuidInput = String.format("%s:%s:%s",serverPreferenceVo.getUserId(),serverPreferenceVo.getServerUrl(),Long.toString(System.currentTimeMillis()));
        String conferenceId = UUID.nameUUIDFromBytes(uuidInput.getBytes()).toString();
        return GoogleConferenceEntity.builder()
                .eventId(form.getCalendarEvent().getEventId())
                .status(ConferenceStatus.ACTIVE)
                .serverPreferenceId(serverPreferenceVo.getId())
                .roomId(conferenceId)
                .build();
    }
}
