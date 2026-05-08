package com.teenthofabud.videoconference.mdr.user.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.form.UserForm;
import com.teenthofabud.videoconference.mdr.user.vo.UserVo;


public interface UserService {

    UserVo createNewUser(UserForm form) throws UserException;

    UserVo getUserById(Long id) throws UserException;

    UserVo getUserByEmail(String email) throws UserException;

    void patchUser(String userId, JsonPatch patches) throws UserException, JsonPatchException, JsonProcessingException;

    void deleteUser(String userId) throws UserException;
}
