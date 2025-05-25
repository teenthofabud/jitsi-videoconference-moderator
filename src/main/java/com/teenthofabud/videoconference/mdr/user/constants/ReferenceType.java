package com.teenthofabud.videoconference.mdr.user.constants;

import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import java.util.NoSuchElementException;

@FieldNameConstants(onlyExplicitlyIncluded = true)
@Getter
public enum ReferenceType {

    GMAIL;

    @Override
    public String toString() {
        return name();
    }

    public static ReferenceType getByName(String name) throws NoSuchElementException {
        ReferenceType x = null;
        for(ReferenceType t : ReferenceType.values()) {
            if(t.name().compareTo(name) == 0) {
                x = t;
                break;
            }
        }
        if(x != null) {
            return x;
        } else {
            throw new NoSuchElementException("No Reference Type available by name: " + name);
        }
    }
}
