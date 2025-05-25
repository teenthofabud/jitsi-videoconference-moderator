package com.teenthofabud.videoconference.mdr.conference.converter;

import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import jakarta.persistence.AttributeConverter;

public class ConferenceStatusAttributeConverter implements AttributeConverter<ConferenceStatus,String> {
    @Override
    public String convertToDatabaseColumn(ConferenceStatus attribute) {
        return attribute.name();
    }

    @Override
    public ConferenceStatus convertToEntityAttribute(String dbData) {
        return ConferenceStatus.getByName(dbData);
    }
}
