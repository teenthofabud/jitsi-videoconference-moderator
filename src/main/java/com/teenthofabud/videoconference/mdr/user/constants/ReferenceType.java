package com.teenthofabud.videoconference.mdr.user.constants;

import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;

import java.util.Arrays;
import java.util.NoSuchElementException;

@FieldNameConstants(onlyExplicitlyIncluded = true)
@Getter
public enum ReferenceType {

    EMAIL;

    @Override
    public String toString() {
        return name();
    }

    public static ReferenceType getByName(String name) throws NoSuchElementException {
        return Arrays.stream(ReferenceType.values()).filter(e -> e.name().equals(name)).findFirst().orElseThrow(() -> new NoSuchElementException("No such Reference type available by name : " + name));
    }

}