package com.teenthofabud.videoconference.mdr.conference.constants.google;

import com.teenthofabud.videoconference.mdr.error.core.MDRError;
import lombok.Getter;

@Getter
public enum GoogleConferenceErrorType implements MDRError {


    AUTHENTICATION,
    //The user is not allowed to use the selected conference solution
    // (but might be allowed to use other solutions offered by the add-on).
    CONFERENCE_SOLUTION_FORBIDDEN,
    PERMANENT,
    PERMISSION_DENIED,
    TEMPORARY,
    UNKNOWN;

    String message;


    GoogleConferenceErrorType(){
        this.message = message;
    }

    @Override
    public String toString() {
        return name();
    }

    @Override
    public String getErrorCode() {
        return this.name();
    }

    @Override
    public Integer getHttpStatusCode() {
        //sending any other http status leads to request failure in Google calendar
        return 200;
    }
}
