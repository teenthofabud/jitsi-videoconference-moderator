package com.teenthofabud.videoconference.moderator.conference.service.impl;

import com.teenthofabud.videoconference.moderator.conference.converter.ConferenceEntityToDtoConverter;
import com.teenthofabud.videoconference.moderator.conference.dto.ConferenceDto;
import com.teenthofabud.videoconference.moderator.conference.entity.ConferenceEntity;
import com.teenthofabud.videoconference.moderator.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.moderator.conference.repository.ConferenceRepository;
import com.teenthofabud.videoconference.moderator.conference.service.ConferenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConferenceServiceImpl implements ConferenceService {

    @Autowired
    ConferenceRepository conferenceRepository;
    @Autowired
    ConferenceEntityToDtoConverter conferenceEntityToDtoConverter;

    @Override
    public ConferenceDto createNewGoogleConference(GoogleConferenceForm form) {
        ConferenceDto conferenceDto = ConferenceDto.builder().build();
        try{
            String calendarId = form.getCalendarEvent().getCalendarId();
            ConferenceEntity conferenceEntity = conferenceRepository.findByUserRef(calendarId);
            if(conferenceEntity!=null){
                conferenceDto = conferenceEntityToDtoConverter.convert(conferenceEntity);
            }
        }
        catch (Exception e){
            log.error("Couldn't create new google conference");
            conferenceDto.setError(e.getMessage());
        }
        return conferenceDto;
    }
}
