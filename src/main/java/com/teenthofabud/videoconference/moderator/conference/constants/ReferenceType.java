package com.teenthofabud.videoconference.moderator.conference.constants;

import lombok.Getter;
import lombok.experimental.FieldNameConstants;

import java.util.NoSuchElementException;

@FieldNameConstants(onlyExplicitlyIncluded = true)
@Getter
public enum ReferenceType {

    EMAIL(0);

    @Override
    public String toString() {
        return name();
    }

    private Integer ordinal;

    ReferenceType(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public static ReferenceType getByOrdinal(Integer ordinal) throws NoSuchElementException {
        ReferenceType x = null;
        for(ReferenceType t : ReferenceType.values()) {
            if(t.getOrdinal().compareTo(ordinal) == 0) {
                x = t;
                break;
            }
        }
        if(x != null) {
            return x;
        } else {
            throw new NoSuchElementException("No User Reference available by ordinal: " + ordinal);
        }
    }

}
