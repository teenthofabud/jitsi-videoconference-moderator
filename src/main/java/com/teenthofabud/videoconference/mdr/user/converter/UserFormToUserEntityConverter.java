package com.teenthofabud.videoconference.mdr.user.converter;

import com.teenthofabud.videoconference.mdr.core.util.UserIdHasher;
import com.teenthofabud.videoconference.mdr.user.constants.ReferenceType;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.form.UserForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class UserFormToUserEntityConverter implements Converter<UserForm, UserEntity> {

    @Autowired
    UserIdHasher userIdHasher;

    @Override
    public UserEntity convert(UserForm source) {
            String userIdHash = userIdHasher.hash(source.getUserId());
            return UserEntity.builder()
                    .userId(source.getUserId())
                    .name(source.getName())
                    .userIdHash(userIdHash)
                    .referenceType(source.getReferenceType())
                    .build();


    }
}
