package com.teenthofabud.videoconference.mdr.user.converter;

import com.teenthofabud.videoconference.mdr.user.constants.ReferenceType;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.form.UserForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserFormToUserEntityConverter implements Converter<UserForm, UserEntity> {

    @Override
    public UserEntity convert(UserForm source) {

            return UserEntity.builder()
                    .userId(source.getUserId())
                    .name(source.getName())
                    .referenceType(source.getReferenceType())
                    .build();


    }
}
