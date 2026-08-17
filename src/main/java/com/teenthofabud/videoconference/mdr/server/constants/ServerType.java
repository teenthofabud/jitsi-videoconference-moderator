package com.teenthofabud.videoconference.mdr.server.constants;

import lombok.Getter;

import java.util.Arrays;
import java.util.NoSuchElementException;

@Getter
public enum ServerType {

    CUSTOM,
    DEFAULT;

    @Override
    public String toString() {
        return name();
    }

    public static ServerType getByName(String name) throws NoSuchElementException {
        return Arrays.stream(ServerType.values()).filter(e->e.name().equals(name))
                .findFirst()
                .orElseThrow(()-> new NoSuchElementException("No Server type available by name : " + name));
    }
}


