package com.teenthofabud.videoconference.mdr.conference.constants.google;

import lombok.Getter;

@Getter
public enum GoogleConferenceErrorType {


    AUTHENTICATION("Authentication error occurred during conference data generation"),
    //The user is not allowed to use the selected conference solution
    // (but might be allowed to use other solutions offered by the add-on).
    CONFERENCE_SOLUTION_FORBIDDEN("User %s is not allowed to use the selected conference solution"),
    PERMANENT("Permanent error occurred during conference data generation"),
    PERMISSION_DENIED("User %s isn't allowed to perform an action in the third-party conferencing system"),
    TEMPORARY("Temporary error occurred during conference data generation"),
    UNKNOWN("Unknown error during conference data generation");

    String message;


    GoogleConferenceErrorType(String message){
        this.message = message;
    }

    @Override
    public String toString() {
        return name();
    }

}
