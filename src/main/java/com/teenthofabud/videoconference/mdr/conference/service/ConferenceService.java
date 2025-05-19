package com.teenthofabud.videoconference.mdr.conference.service;

import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.error.google.UserNotFoundException;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;

public interface ConferenceService {

    GoogleConferenceDataVo createNewGoogleConference(GoogleConferenceForm form) throws UserNotFoundException;

}
