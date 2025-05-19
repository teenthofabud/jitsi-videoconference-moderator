package com.teenthofabud.videoconference.mdr.conference.service.impl;

import com.teenthofabud.videoconference.mdr.conference.converter.google.ConferenceEntityToGoogeConferenceDataVoConverter;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.entity.ConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.error.google.UserNotFoundException;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.repository.ConferenceRepository;
import com.teenthofabud.videoconference.mdr.conference.service.ConferenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConferenceServiceImpl implements ConferenceService {

    @Autowired
    ConferenceRepository conferenceRepository;
    @Autowired
    ConferenceEntityToGoogeConferenceDataVoConverter conferenceEntityToGoogeConferenceDataVoConverter;

    @Override
    public GoogleConferenceDataVo createNewGoogleConference(GoogleConferenceForm form) throws UserNotFoundException {
        GoogleConferenceDataVo googleConferenceDataVo = GoogleConferenceDataVo.builder().build();

        String calendarId = form.getCalendarEvent().getCalendarId();
        ConferenceEntity conferenceEntity = conferenceRepository.findByUserRef(calendarId);
        if(conferenceEntity!=null){
            googleConferenceDataVo = conferenceEntityToGoogeConferenceDataVoConverter.convert(conferenceEntity);
        }
        else throw new UserNotFoundException();
        return googleConferenceDataVo;
    }
}
