package com.teenthofabud.videoconference.mdr.conference.error.google;

import com.teenthofabud.videoconference.mdr.conference.constants.google.GoogleConferenceErrorType;
import com.teenthofabud.videoconference.mdr.error.core.MDRBaseException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GoogleConferenceException extends MDRBaseException {

    GoogleConferenceErrorType errorType;
    Object[] parameters;

    public GoogleConferenceException(GoogleConferenceErrorType errorType, Object[] parameters){
        super(errorType,parameters);
        this.errorType = errorType;
        this.parameters = parameters;
    }

}
