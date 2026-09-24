package com.teenthofabud.videoconference.mdr.conference.google.error;

import com.teenthofabud.videoconference.mdr.conference.google.constants.GoogleConferenceErrorType;
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

    public GoogleConferenceException(GoogleConferenceErrorType errorType,String message){
        super(errorType,message);
        this.errorType = errorType;
    }

}
