package com.teenthofabud.videoconference.mdr.user.converter;

import com.teenthofabud.videoconference.mdr.user.vo.UserVo;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserEntityToVoConverter implements Converter<UserEntity, UserVo> {


    @Override
    public UserVo convert(UserEntity source) {
        return UserVo.builder()
                .userIdHash(source.getUserIdHash())
                .userId(source.getUserId())
                .id(source.getId())
                .name(source.getName())
                .referenceType(source.getReferenceType())
                .build();
    }
}
