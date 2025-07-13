package com.teenthofabud.videoconference.mdr.user.converter;

import com.teenthofabud.videoconference.mdr.user.constants.ReferenceType;
import jakarta.persistence.AttributeConverter;

public class ReferenceTypeAttributeConverter implements AttributeConverter<ReferenceType,String> {

    @Override
    public String convertToDatabaseColumn(ReferenceType attribute) {
        return attribute.name();
    }

    @Override
    public ReferenceType convertToEntityAttribute(String dbData) {
        return ReferenceType.getByName(dbData);
    }
}
