package com.teenthofabud.videoconference.mdr.error.core;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class MDRBaseException extends Exception {

    private transient MDRError error;

    protected MDRBaseException(MDRError error, String message) {
        super(message);
        this.error = error;
    }

}
