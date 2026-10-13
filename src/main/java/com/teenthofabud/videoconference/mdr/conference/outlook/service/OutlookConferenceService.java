package com.teenthofabud.videoconference.mdr.conference.outlook.service;

import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.conference.outlook.form.OutlookAppointmentSentForm;
import com.teenthofabud.videoconference.mdr.conference.outlook.form.OutlookConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.outlook.vo.OutlookConferenceVo;
import com.teenthofabud.videoconference.mdr.conference.outlook.vo.OutlookConferenceSetupVo;

public interface OutlookConferenceService {

    OutlookConferenceSetupVo getSetup(String email) throws ConferenceException;

    OutlookConferenceVo createConference(OutlookConferenceForm form) throws ConferenceException;

    OutlookConferenceVo markAppointmentSent(Long userId, String draftTrackingId, OutlookAppointmentSentForm form)
            throws ConferenceException;

    void cancelConference(Long userId, String draftTrackingId) throws ConferenceException;
}
