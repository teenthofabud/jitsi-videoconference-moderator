package com.teenthofabud.videoconference.mdr.conference.constants;

import lombok.Getter;
import lombok.experimental.FieldNameConstants;

import java.util.NoSuchElementException;

@Getter
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum ConferenceStatus {

    ACTIVE,
    INACTIVE;

    @Override
    public String toString() {
        return name();
    }

    public static ConferenceStatus getByName(String name) throws NoSuchElementException {
        for (ConferenceStatus status : ConferenceStatus.values()) {
            if (status.name().equals(name)) {
                return status;
            }
        }
        throw new NoSuchElementException("No Conference Status available by name: " + name);
    }

    public static String getName(ConferenceStatus status) {
        return status != null ? status.name() : null;
    }
}
