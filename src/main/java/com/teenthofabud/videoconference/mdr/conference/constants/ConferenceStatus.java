package com.teenthofabud.videoconference.mdr.conference.constants;

import lombok.Getter;
import lombok.experimental.FieldNameConstants;

import java.util.Arrays;
import java.util.NoSuchElementException;

@Getter
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum ConferenceStatus {

    ACTIVE,
    PENDING,
    CANCELLED;

    @Override
    public String toString() {
        return name();
    }

    public static ConferenceStatus getByName(String name) throws NoSuchElementException {

        return Arrays.stream(ConferenceStatus.values()).filter(e->e.name().equals(name)).findFirst().orElseThrow(()-> new NoSuchElementException("No Conference status available by name : "+name));
    }

}
