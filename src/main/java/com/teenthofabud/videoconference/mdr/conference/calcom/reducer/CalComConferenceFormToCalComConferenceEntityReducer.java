package com.teenthofabud.videoconference.mdr.conference.calcom.reducer;

import com.teenthofabud.videoconference.mdr.conference.calcom.entity.CalComConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.calcom.form.CalComConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CalComConferenceFormToCalComConferenceEntityReducer {

    public CalComConferenceEntity reduce(CalComConferenceForm form) {
        return CalComConferenceEntity.builder()
                .bookingUid(form.getBookingUid())
                .bookingId(form.getBookingId())
                .userId(form.getUserId())
                .serverId(form.getServerId())
                .roomId(UUID.randomUUID().toString())
                .status(ConferenceStatus.ACTIVE)
                .build();
    }
}
