package com.teenthofabud.videoconference.mdr.user.converter;

import com.teenthofabud.videoconference.mdr.serverpreference.error.ServerPreferenceException;
import com.teenthofabud.videoconference.mdr.serverpreference.service.ServerPreferenceService;
import com.teenthofabud.videoconference.mdr.serverpreference.vo.ServerPreferenceVo;
import com.teenthofabud.videoconference.mdr.user.dto.UserDto;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class UserEntityToDtoConverter implements Converter<UserEntity, Optional<UserDto>> {

    @Autowired
    ServerPreferenceService serverPreferenceService;

    @Override
    public Optional<UserDto> convert(UserEntity source) {

        try {
            ServerPreferenceVo serverPreferenceVo = serverPreferenceService.getServerPreferenceByUserId(source.getUserId());
            return Optional.of(UserDto.builder()
                    .userId(source.getUserId())
                    .name(source.getName())
                    .preferredServerUrl(serverPreferenceVo.getServerUrl())
                    .build());

        } catch (ServerPreferenceException e) {
            return Optional.empty();
        }

    }
}
