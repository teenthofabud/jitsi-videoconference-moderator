package com.teenthofabud.videoconference.mdr.conference.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceVo;
import jakarta.json.JsonArray;
import jakarta.json.JsonPatch;

import java.util.List;

public interface GoogleConferenceService {

    GoogleConferenceDataVo createNewGoogleConference(GoogleConferenceForm form) throws GoogleConferenceException;

    GoogleConferenceVo getGoogleConferenceByUserIdAndEventId(Long userId, String eventId) throws GoogleConferenceException;

    List<GoogleConferenceVo> getAllGoogleConferencesByUserId(Long userId) throws GoogleConferenceException;

    void patchGoogleConferenceByUserIdAndEventId(Long userId, String eventId, String patches) throws GoogleConferenceException, JsonProcessingException;

    void deleteGoogleConferenceByUserIdAndEventId(Long userId, String eventId) throws GoogleConferenceException;

}
