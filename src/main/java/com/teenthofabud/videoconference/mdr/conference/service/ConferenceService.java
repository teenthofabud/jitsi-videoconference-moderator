package com.teenthofabud.videoconference.mdr.conference.service;

import com.github.fge.jsonpatch.JsonPatch;
import com.teenthofabud.videoconference.mdr.conference.vo.ConferenceVo;
import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleCalendarEvent;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.serverpreference.error.ServerPreferenceException;
import com.teenthofabud.videoconference.mdr.user.error.UserException;

import java.util.List;

public interface ConferenceService {

    GoogleConferenceDataVo createNewGoogleConference(GoogleConferenceForm form) throws GoogleConferenceException;

    ConferenceVo getGoogleConferenceById(Long conferenceId) throws GoogleConferenceException;

    List<ConferenceVo> getAllGoogleConferencesByUserId(String userId) throws GoogleConferenceException;

    void patchGoogleConferenceById(Long conferenceId, GoogleCalendarEvent calendarEvent);


    void deleteGoogleConferenceById(Long conferenceId) throws GoogleConferenceException;

}
