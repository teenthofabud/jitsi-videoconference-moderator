package com.teenthofabud.videoconference.mdr.user.error;

import com.teenthofabud.videoconference.mdr.error.core.MDRBaseException;
import com.teenthofabud.videoconference.mdr.error.core.MDRError;

public class UserException extends MDRBaseException {
    public UserException(MDRError error, String message) {
        super(error, message);
    }
}
