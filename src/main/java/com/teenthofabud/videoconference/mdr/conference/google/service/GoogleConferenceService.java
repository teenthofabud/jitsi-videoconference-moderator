package com.teenthofabud.videoconference.mdr.conference.google.service;

import jakarta.json.JsonPatch;
import com.teenthofabud.videoconference.mdr.conference.google.error.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.google.form.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.google.vo.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.google.vo.GoogleConferenceVo;

import java.util.List;

public interface GoogleConferenceService {

    GoogleConferenceDataVo createNewGoogleConference(GoogleConferenceForm form) throws GoogleConferenceException;

    GoogleConferenceVo getGoogleConferenceByUserIdAndEventId(Long userId, String eventId) throws GoogleConferenceException;

    List<GoogleConferenceVo> getAllGoogleConferencesByUserId(Long userId) throws GoogleConferenceException;

    void patchGoogleConferenceByUserIdAndEventId(Long userId, String eventId, JsonPatch patches) throws GoogleConferenceException;

    void deleteGoogleConferenceByUserIdAndEventId(Long userId, String eventId) throws GoogleConferenceException;

}
