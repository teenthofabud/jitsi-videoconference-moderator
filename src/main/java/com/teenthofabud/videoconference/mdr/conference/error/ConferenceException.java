package com.teenthofabud.videoconference.mdr.conference.error;

import com.teenthofabud.videoconference.mdr.error.core.MDRBaseException;
import com.teenthofabud.videoconference.mdr.error.core.MDRError;

public class ConferenceException extends MDRBaseException {

    public ConferenceException(MDRError error, String message) {
        super(error, message);
    }
}
