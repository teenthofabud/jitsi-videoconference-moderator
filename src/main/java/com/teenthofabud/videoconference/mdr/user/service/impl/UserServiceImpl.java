package com.teenthofabud.videoconference.mdr.user.service.impl;

import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.user.converter.UserEntityToDtoConverter;
import com.teenthofabud.videoconference.mdr.user.converter.UserFormToUserEntityConverter;
import com.teenthofabud.videoconference.mdr.user.dto.UserDto;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.form.UserForm;
import com.teenthofabud.videoconference.mdr.user.repository.UserRepository;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserFormToUserEntityConverter userFormToUserEntityConverter;
    @Autowired
    UserEntityToDtoConverter userEntityToDtoConverter;

    @Override
    public void createNewUser(UserForm form) throws UserException {

        Optional<UserEntity> existingUser = userRepository.findByUserId(form.getUserId());
        if(existingUser.isPresent())
            throw new UserException(MDRErrorCode.MDR_EXISTS,new Object[]{form.getUserId()});
        UserEntity userEntity = userFormToUserEntityConverter.convert(form);
        userRepository.save(userEntity);

    }

    @Override
    public UserDto getUserById(String userId) throws UserException {
        return userRepository.findByUserId(userId).map(e->userEntityToDtoConverter.convert(e)).orElseThrow(()->new UserException(MDRErrorCode.MDR_NOT_FOUND,new Object[]{userId}));
    }


}
