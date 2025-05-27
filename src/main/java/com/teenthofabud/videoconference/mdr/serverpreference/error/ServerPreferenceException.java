package com.teenthofabud.videoconference.mdr.serverpreference.error;

import com.teenthofabud.videoconference.mdr.error.core.MDRBaseException;
import com.teenthofabud.videoconference.mdr.error.core.MDRError;

public class ServerPreferenceException extends MDRBaseException {
    public ServerPreferenceException(MDRError error, Object[] parameters) {
        super(error, parameters);
    }
}
