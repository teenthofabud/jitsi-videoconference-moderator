package com.teenthofabud.videoconference.mdr.user.converter;

import com.teenthofabud.videoconference.mdr.user.dto.UserDto;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserEntityToDtoConverter implements Converter<UserEntity, UserDto> {


    @Override
    public UserDto convert(UserEntity source) {
        return UserDto.builder()
                .userId(source.getUserId())
                .name(source.getName())
                .referenceType(source.getReferenceType().name())
                .build();
    }
}
