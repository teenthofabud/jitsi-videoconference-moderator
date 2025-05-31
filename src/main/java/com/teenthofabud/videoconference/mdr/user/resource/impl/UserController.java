package com.teenthofabud.videoconference.mdr.user.resource.impl;

import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.user.dto.UserDto;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.form.UserForm;
import com.teenthofabud.videoconference.mdr.user.resource.UserAPI;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController implements UserAPI {
    @Autowired
    UserService userService;

    @Override
    public ResponseEntity<Void> createNewUser(@Valid UserForm form) throws UserException {
        userService.createNewUser(form);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(form.getUserId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @Override
    public ResponseEntity<UserDto> getUserById(String userId) throws UserException {
        UserDto userDto = userService.getUserById(userId);
        return ResponseEntity.ok(userDto);
    }
}
