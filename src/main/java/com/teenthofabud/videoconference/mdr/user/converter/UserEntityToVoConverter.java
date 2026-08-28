package com.teenthofabud.videoconference.mdr.user.converter;

import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.user.vo.UserVo;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserEntityToVoConverter implements Converter<com.teenthofabud.videoconference.mdr.user.entity.UserEntity, UserVo> {

    @Override
    public UserVo convert(UserEntity source) {
        return UserVo.builder()
                .email(source.getEmail())
                .id(source.getId())
                .name(source.getName())
//                .referenceType(source.getReferenceType())
                .preferredServerUrl(Optional.ofNullable(source.getPreferredServer())
                        .map(ServerEntity::getServerUrl)
                        .orElse(null))
                .isManaged(source.getManaged())
                .build();
    }
}
