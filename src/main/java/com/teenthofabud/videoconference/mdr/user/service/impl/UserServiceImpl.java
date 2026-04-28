package com.teenthofabud.videoconference.mdr.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.form.ServerForm;
import com.teenthofabud.videoconference.mdr.server.service.ServerService;
import com.teenthofabud.videoconference.mdr.user.converter.UserFormToUserEntityConverter;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.form.ServerPreferenceForm;
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
    ObjectMapper mapper;
    @Autowired
    ServerService serverService;

    @Override
    public UserEntity createNewUser(UserForm form) throws UserException {
        Optional<UserEntity> existingUser = userRepository.findByEmail(form.getEmail());
        if(existingUser.isPresent())
            throw new UserException(MDRErrorCode.MDR_EXISTS,new Object[]{form.getEmail()});
        com.teenthofabud.videoconference.mdr.user.entity.UserEntity userEntity = userFormToUserEntityConverter.convert(form);
        userRepository.save(userEntity);
        return userEntity;
    }

    @Override
    public UserEntity getUserById(Long id) throws UserException {
        return userRepository.findById(id)
                .orElseThrow(()->new UserException(MDRErrorCode.MDR_NOT_FOUND,new Object[]{id}));
    }

    @Override
    public UserEntity getUserByEmail(String email) throws UserException {
        return userRepository.findByEmail(email)
                .orElseThrow(()->new UserException(MDRErrorCode.MDR_NOT_FOUND,new Object[]{email}));
    }

    @Override
    public void addServerPreference(ServerPreferenceForm form) throws UserException, ServerException {
        UserEntity user = userRepository.findByEmail(form.getEmail())
                .orElseThrow(() -> new UserException(MDRErrorCode.MDR_NOT_FOUND,
                        new Object[]{form.getEmail()}));
        ServerForm serverForm = ServerForm.builder()
                .serverUrl(form.getPreferredServerUrl())
                .name(form.getName())
                .build();
        Optional<ServerEntity> existingServer = serverService.findServerByUrl(form.getPreferredServerUrl());
        ServerEntity server = existingServer.isPresent() ? existingServer.get() :
                serverService.createNewCustomServer(serverForm);
        server.setOwner(user);
        userRepository.save(user);
    }

    @Override
    public void modifyServerPreference(ServerPreferenceForm form) throws UserException, ServerException {
        UserEntity user = userRepository.findByEmail(form.getEmail())
                .orElseThrow(() -> new UserException(MDRErrorCode.MDR_NOT_FOUND,
                        new Object[]{form.getEmail()}));
        ServerForm serverForm = ServerForm.builder()
                .serverUrl(form.getPreferredServerUrl())
                .name(form.getName())
                .build();
        ServerEntity server = serverService.createNewCustomServer(serverForm);
        server.setOwner(user);
        userRepository.save(user);
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
