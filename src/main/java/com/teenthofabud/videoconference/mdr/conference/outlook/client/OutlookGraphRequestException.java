package com.teenthofabud.videoconference.mdr.conference.outlook.client;

/** Represents a non-successful HTTP response from Microsoft Graph. */
public class OutlookGraphRequestException extends Exception {
    private final int statusCode;

    public OutlookGraphRequestException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    /** Returns the HTTP status returned by Microsoft Graph. */
    public int getStatusCode() {
        return statusCode;
    }
}
