package com.teenthofabud.videoconference.mdr.conference.service;

import jakarta.json.JsonPatch;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceVo;

import java.util.List;

public interface GoogleConferenceService {

    GoogleConferenceDataVo createNewGoogleConference(GoogleConferenceForm form) throws GoogleConferenceException;

    GoogleConferenceVo getGoogleConferenceByUserIdAndEventId(Long userId, String eventId) throws GoogleConferenceException;

    List<GoogleConferenceVo> getAllGoogleConferencesByUserId(Long userId) throws GoogleConferenceException;

    void patchGoogleConferenceByUserIdAndEventId(Long userId, String eventId, JsonPatch patches) throws GoogleConferenceException, JsonProcessingException;

    void deleteGoogleConferenceByUserIdAndEventId(Long userId, String eventId) throws GoogleConferenceException;

}
