package com.teenthofabud.videoconference.mdr.user.service;


import com.teenthofabud.videoconference.mdr.user.dto.UserDto;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.form.UserForm;

public interface UserService {

    void createNewUser(UserForm form) throws UserException;

    UserDto getUserById(String userId) throws UserException;
}
