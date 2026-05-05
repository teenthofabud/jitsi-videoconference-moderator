package com.teenthofabud.videoconference.mdr.conference.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceVo;

import java.util.List;

public interface GoogleConferenceService {

    GoogleConferenceDataVo createNewGoogleConference(GoogleConferenceForm form) throws GoogleConferenceException;

    GoogleConferenceVo getGoogleConferenceByUserEmailAndEventId(String email, String eventId) throws GoogleConferenceException;

    List<GoogleConferenceVo> getAllGoogleConferencesByUserEmail(String email) throws GoogleConferenceException;

    void patchGoogleConferenceByUserEmailAndEventId(String email, String eventId, JsonPatch patches) throws GoogleConferenceException, JsonPatchException, JsonProcessingException;

    void deleteGoogleConferenceByUserEmailAndEventId(String email, String eventId) throws GoogleConferenceException;

}
