package com.teenthofabud.videoconference.mdr.server.error;

import com.teenthofabud.videoconference.mdr.error.core.MDRBaseException;
import com.teenthofabud.videoconference.mdr.error.core.MDRError;

public class ServerException extends MDRBaseException {
    public ServerException(MDRError error, String message) {
        super(error, message);
    }
}
