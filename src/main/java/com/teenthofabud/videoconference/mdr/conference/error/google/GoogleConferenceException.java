package com.teenthofabud.videoconference.mdr.conference.error.google;

import com.teenthofabud.videoconference.mdr.conference.constants.google.GoogleConferenceErrorType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GoogleConferenceException extends Exception{

    GoogleConferenceErrorType errorType;
    Object[] parameters;

    public GoogleConferenceException(GoogleConferenceErrorType errorType, Object[] parameters){
        this.errorType = errorType;
        this.parameters = parameters;
    }

}
