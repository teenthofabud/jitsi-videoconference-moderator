package com.teenthofabud.videoconference.mdr.conference.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceVo;

import java.util.List;

public interface GoogleConferenceService {

    GoogleConferenceDataVo createNewGoogleConference(GoogleConferenceForm form) throws GoogleConferenceException;

    GoogleConferenceVo getGoogleConferenceByUserAndEventId(String eventId, String userId) throws GoogleConferenceException;

    List<GoogleConferenceVo> getAllGoogleConferencesByUserId(String userId) throws GoogleConferenceException;

    void patchGoogleConferenceByUserAndEventId(String eventId, String userId, JsonPatch patches) throws GoogleConferenceException, JsonPatchException, JsonProcessingException;


    void deleteGoogleConferenceByUserAndEventId(String eventId, String userId) throws GoogleConferenceException;

}
