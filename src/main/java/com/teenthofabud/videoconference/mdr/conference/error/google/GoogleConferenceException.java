package com.teenthofabud.videoconference.mdr.conference.error.google;

import com.teenthofabud.videoconference.mdr.conference.constants.google.GoogleConferenceErrorType;
import com.teenthofabud.videoconference.mdr.error.core.MDRBaseException;
import com.teenthofabud.videoconference.mdr.error.core.MDRError;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GoogleConferenceException extends MDRBaseException {

    GoogleConferenceErrorType errorType;

    public GoogleConferenceException(GoogleConferenceErrorType errorType,MDRError error, Object[] parameters){
        super(error,parameters);
        this.errorType = errorType;
    }

}
