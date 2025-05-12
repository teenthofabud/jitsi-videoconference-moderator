package com.teenthofabud.videoconference.moderator.conference.constants;

import lombok.Getter;
import lombok.experimental.FieldNameConstants;

import java.util.NoSuchElementException;

@Getter
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum ConferenceStatus {

    ACTIVE(0),
    INACTIVE(1);

    @Override
    public String toString() {
        return name();
    }

    private Integer ordinal;

    ConferenceStatus(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public static ConferenceStatus getByOrdinal(Integer ordinal) throws NoSuchElementException {
        ConferenceStatus x = null;
        for(ConferenceStatus t : ConferenceStatus.values()) {
            if(t.getOrdinal().compareTo(ordinal) == 0) {
                x = t;
                break;
            }
        }
        if(x != null) {
            return x;
        } else {
            throw new NoSuchElementException("No Conference Status available by ordinal: " + ordinal);
        }
    }

}
