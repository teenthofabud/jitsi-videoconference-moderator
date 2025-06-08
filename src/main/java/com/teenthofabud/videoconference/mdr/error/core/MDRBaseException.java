package com.teenthofabud.videoconference.mdr.error.core;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class MDRBaseException extends Exception {

    private transient MDRError error;
    private transient Object[] parameters;

    protected MDRBaseException(MDRError error, Object[] parameters) {
        super(error.getErrorCode());
        this.error = error;
        this.parameters = parameters;
    }


}
