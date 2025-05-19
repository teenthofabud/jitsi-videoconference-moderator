package com.teenthofabud.videoconference.mdr.conference.constants.google;

public enum GoogleConferenceErrorType {

    AUTHENTICATION("Authentication error occurred during conference data generation"),
    CONFERENCE_SOLUTION_FORBIDDEN("This user is not allowed to use the selected conference solution"),
    PERMANENT("Permanent error occurred during conference data generation"),
    PERMISSION_DENIED("This user isn't allowed to perform an action in the third-party conferencing system"),
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
