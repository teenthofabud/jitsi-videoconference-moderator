package com.teenthofabud.videoconference.mdr.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.user.converter.UserEntityToVoConverter;
import com.teenthofabud.videoconference.mdr.user.converter.UserFormToUserEntityConverter;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.form.UserForm;
import com.teenthofabud.videoconference.mdr.user.repository.UserRepository;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import com.teenthofabud.videoconference.mdr.user.vo.UserVo;
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
    ObjectMapper mapper;
    @Autowired
    UserEntityToVoConverter userEntityToVoConverter;

    @Override
    public UserVo createNewUser(UserForm form, Boolean isManaged) throws UserException {
        Optional<UserEntity> existingUser = userRepository.findByEmail(form.getEmail());
        if(existingUser.isPresent()){
            log.error("User already exists with email id {}",form.getEmail());
            throw new UserException(MDRErrorCode.MDR_EXISTS,String.format("User already exists with email %s",form.getEmail()));
        }
        UserEntity userEntity = userFormToUserEntityConverter.convert(form);
        userEntity.setManaged(isManaged);
        userRepository.save(userEntity);
        return userEntityToVoConverter.convert(userEntity);
    }

    @Override
    public UserVo getUserById(Long id) throws UserException {
        return userRepository.findById(id)
                .map(userEntityToVoConverter::convert)
                .orElseThrow(() -> {
                    log.error("User doesn't exist with id {}",id);
                    return new UserException(
                            MDRErrorCode.MDR_NOT_FOUND,
                            String.format("User doesn't exist with id %s",id)
                    );
                });
    }

    @Override
    public UserVo getUserByEmail(String email) throws UserException {
        return userRepository.findByEmail(email)
                .map(userEntityToVoConverter::convert)
                .orElseThrow(() -> {
                    log.error("User doesn't exist with email {}",email);
                    return new UserException(
                            MDRErrorCode.MDR_NOT_FOUND,
                            String.format("User doesn't exist with email %s",email)
                    );
                });
    }

    @Override
    public void patchUser(String email, JsonPatch patches) throws UserException, JsonPatchException, JsonProcessingException {
//        UserEntity userEntity = userRepository.findByUserId(userId).orElseThrow(()-> new UserException(MDRErrorCode.MDR_NOT_FOUND,new Object[]{userId}));
//        UserDto userDto = null;
//        JsonNode original = mapper.valueToTree(userDto);
//        JsonNode patched = patches.apply(original);
//        UserDto patchedUserDto = mapper.treeToValue(patched,UserDto.class);
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void deleteUser(String userId) throws UserException {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
