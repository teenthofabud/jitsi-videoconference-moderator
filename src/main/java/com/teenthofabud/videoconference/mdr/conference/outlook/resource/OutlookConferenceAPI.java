package com.teenthofabud.videoconference.mdr.conference.outlook.resource;

import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.conference.outlook.form.OutlookAppointmentSentForm;
import com.teenthofabud.videoconference.mdr.conference.outlook.form.OutlookConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.outlook.vo.OutlookConferenceVo;
import com.teenthofabud.videoconference.mdr.conference.outlook.vo.OutlookConferenceSetupVo;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

public interface OutlookConferenceAPI {

    ResponseEntity<OutlookConferenceSetupVo> getSetup(String email) throws ConferenceException;

    ResponseEntity<OutlookConferenceVo> createConference(@Valid OutlookConferenceForm form) throws ConferenceException;

    ResponseEntity<OutlookConferenceVo> markAppointmentSent(String draftTrackingId, Long userId,
            @Valid OutlookAppointmentSentForm form) throws ConferenceException;

    ResponseEntity<Void> cancelConference(String draftTrackingId, Long userId) throws ConferenceException;
}
