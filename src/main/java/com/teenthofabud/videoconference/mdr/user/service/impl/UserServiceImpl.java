package com.teenthofabud.videoconference.mdr.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.user.converter.UserEntityToVoConverter;
import com.teenthofabud.videoconference.mdr.user.converter.UserFormToUserEntityConverter;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.form.UserForm;
import com.teenthofabud.videoconference.mdr.user.repository.UserRepository;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import com.teenthofabud.videoconference.mdr.user.vo.UserVo;
import jakarta.json.JsonObject;
import jakarta.json.JsonPatch;
import jakarta.json.JsonValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserFormToUserEntityConverter userFormToUserEntityConverter;
    private final ObjectMapper mapper;
    private final UserEntityToVoConverter userEntityToVoConverter;

    @Override
    public UserVo createNewUser(UserForm form, Boolean isManaged) throws UserException {
        Optional<UserEntity> existingUser = userRepository.findByEmail(form.getEmail());
        if(existingUser.isPresent()){
            log.error("User already exists with email id {}",form.getEmail());
            throw new UserException(MDRErrorCode.MDR_EXISTS,"You are already on the waitlist!");
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
    public void patchUser(Long userId, JsonPatch patches) throws UserException, JsonProcessingException {
//        UserEntity userEntity = userRepository.findById(userId).orElseThrow(()-> new UserException(MDRErrorCode.MDR_NOT_FOUND,String.format("User with ID %s not found",userId));
//        // Convert the Spring entity into a Jakarta JsonValue using Jackson 3
//        JsonValue source = mapper.convertValue(userEntity, JsonValue.class);
//        // Apply the JSON-P patch to the JsonObject
//        JsonObject patched = patches.apply(source.asJsonObject());
//        // Convert the patched JsonObject back into the Spring entity
//        userEntity = mapper.convertValue(patched, UserEntity.class);
//        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void deleteUser(String userId) throws UserException {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
