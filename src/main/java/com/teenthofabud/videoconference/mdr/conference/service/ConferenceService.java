package com.teenthofabud.videoconference.mdr.conference.service;

import com.github.fge.jsonpatch.JsonPatch;
import com.teenthofabud.videoconference.mdr.conference.dto.ConferenceDto;
import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleCalendarEvent;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.serverpreference.error.ServerPreferenceException;
import com.teenthofabud.videoconference.mdr.user.error.UserException;

import java.util.List;

public interface ConferenceService {

    GoogleConferenceDataVo createNewGoogleConference(GoogleConferenceForm form) throws GoogleConferenceException, UserException, ServerPreferenceException;

    ConferenceDto getConferenceById(Long conferenceId) throws ConferenceException;

    List<ConferenceDto> getAllConferencesByUserId(String userId);

    void patchGoogleConferenceById(Long conferenceId, GoogleCalendarEvent calendarEvent);

    void patchConferenceById(Long conferenceId, JsonPatch patches);

    void deleteConferenceById(Long conferenceId) throws ConferenceException;

}
