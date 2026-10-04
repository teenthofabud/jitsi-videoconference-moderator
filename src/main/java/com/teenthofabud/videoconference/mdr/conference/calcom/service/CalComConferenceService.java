package com.teenthofabud.videoconference.mdr.conference.calcom.service;

import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.conference.calcom.form.CalComConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.calcom.vo.CalComConferenceVo;
import jakarta.json.JsonPatch;

import java.util.List;

public interface CalComConferenceService {

    CalComConferenceVo createConference(CalComConferenceForm form) throws ConferenceException;

    CalComConferenceVo getConference(Long userId, String bookingUid) throws ConferenceException;

    List<CalComConferenceVo> getAllConferencesByUserId(Long userId) throws ConferenceException;

    void patchConference(Long userId, String bookingUid, JsonPatch patches) throws ConferenceException;

    void deleteConference(Long userId, String bookingUid) throws ConferenceException;
}
