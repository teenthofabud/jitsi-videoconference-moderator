package com.teenthofabud.videoconference.mdr.user.converter;

import com.teenthofabud.videoconference.mdr.user.vo.UserVo;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserEntityToVoConverter implements Converter<com.teenthofabud.videoconference.mdr.user.entity.UserEntity, UserVo> {


    @Override
    public UserVo convert(com.teenthofabud.videoconference.mdr.user.entity.UserEntity source) {
        return UserVo.builder()
                .email(source.getEmail())
                .id(source.getId())
                .name(source.getName())
                .referenceType(source.getReferenceType())
                .build();
    }
}
