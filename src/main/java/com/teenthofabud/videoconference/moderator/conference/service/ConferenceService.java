package com.teenthofabud.videoconference.moderator.conference.service;

import com.teenthofabud.videoconference.moderator.conference.dto.ConferenceDto;
import com.teenthofabud.videoconference.moderator.conference.form.google.GoogleConferenceForm;

public interface ConferenceService {

    ConferenceDto createNewGoogleConference(GoogleConferenceForm form);

}
