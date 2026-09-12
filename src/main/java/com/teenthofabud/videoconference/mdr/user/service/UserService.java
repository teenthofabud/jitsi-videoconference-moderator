package com.teenthofabud.videoconference.mdr.user.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.form.UserForm;
import com.teenthofabud.videoconference.mdr.user.vo.UserVo;
import jakarta.json.JsonPatch;


public interface UserService {

    UserVo createNewUser(UserForm form, Boolean isManaged) throws UserException;

    UserVo getUserById(Long id) throws UserException;

    UserVo getUserByEmail(String email) throws UserException;

    void patchUser(Long userId, JsonPatch patches) throws UserException, JsonProcessingException;

    void deleteUser(String userId) throws UserException;
}
