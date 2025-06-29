package com.teenthofabud.videoconference.mdr.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.serverpreference.error.ServerPreferenceException;
import com.teenthofabud.videoconference.mdr.serverpreference.form.ServerPreferenceForm;
import com.teenthofabud.videoconference.mdr.serverpreference.service.ServerPreferenceService;
import com.teenthofabud.videoconference.mdr.user.converter.UserEntityToDtoConverter;
import com.teenthofabud.videoconference.mdr.user.converter.UserEntityToVoConverter;
import com.teenthofabud.videoconference.mdr.user.converter.UserFormToUserEntityConverter;
import com.teenthofabud.videoconference.mdr.user.vo.UserVo;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.form.UserForm;
import com.teenthofabud.videoconference.mdr.user.repository.UserRepository;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserFormToUserEntityConverter userFormToUserEntityConverter;
    @Autowired
    UserEntityToVoConverter userEntityToVoConverter;
    @Autowired
    UserEntityToDtoConverter userEntityToDtoConverter;
    @Autowired
    ServerPreferenceService serverPreferenceService;
    @Autowired
    ObjectMapper mapper;

    @Override
    @Transactional(rollbackFor = ServerPreferenceException.class)
    public UserVo createNewUser(UserForm form) throws UserException, ServerPreferenceException {
        Optional<UserEntity> existingUser = userRepository.findByUserId(form.getUserId());
        if(existingUser.isPresent())
            throw new UserException(MDRErrorCode.MDR_EXISTS,new Object[]{form.getUserId()});
        UserEntity userEntity = userFormToUserEntityConverter.convert(form);
        userRepository.save(userEntity);
        ServerPreferenceForm serverPreferenceForm = ServerPreferenceForm.builder()
                .serverUrl(form.getPreferredServerUrl())
                .userId(form.getUserId())
                .serverPreferenceType(form.getServerPreferenceType())
                .build();
        serverPreferenceService.createServerPreference(serverPreferenceForm);
        return userEntityToVoConverter.convert(userEntity);
    }

    @Override
    public UserVo getUserById(Long id) throws UserException {
        return userRepository.findById(id).map(e-> userEntityToVoConverter.convert(e)).orElseThrow(()->new UserException(MDRErrorCode.MDR_NOT_FOUND,new Object[]{id}));
    }

    @Override
    public void patchUser(String userId, JsonPatch patches) throws UserException, JsonPatchException, JsonProcessingException {
//        UserEntity userEntity = userRepository.findByUserId(userId).orElseThrow(()-> new UserException(MDRErrorCode.MDR_NOT_FOUND,new Object[]{userId}));
//        UserDto userDto = userEntityToDtoConverter.convert(userEntity).orElseThrow(()-> new UserException(MDRErrorCode.MDR_ATTRIBUTE_INVALID,new Object[]{userEntity.getServerPreferenceId()}));
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
