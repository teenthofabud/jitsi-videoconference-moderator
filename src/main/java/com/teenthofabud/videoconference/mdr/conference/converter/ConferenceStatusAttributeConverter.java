package com.teenthofabud.videoconference.mdr.conference.converter;

import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import jakarta.persistence.AttributeConverter;

public class ConferenceStatusAttributeConverter implements AttributeConverter<ConferenceStatus,Integer> {
    @Override
    public Integer convertToDatabaseColumn(ConferenceStatus attribute) {
        return attribute.ordinal();
    }

    @Override
    public ConferenceStatus convertToEntityAttribute(Integer dbData) {
        return ConferenceStatus.getByOrdinal(dbData);
    }
}
