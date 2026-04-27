package com.teenthofabud.videoconference.mdr.user.resource.impl;

import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.user.converter.UserEntityToVoConverter;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.form.ServerPreferenceForm;
import com.teenthofabud.videoconference.mdr.user.vo.UserVo;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.form.UserForm;
import com.teenthofabud.videoconference.mdr.user.resource.UserAPI;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController implements UserAPI {

    @Autowired
    UserService userService;
    @Autowired
    UserEntityToVoConverter userEntityToVoConverter;

    @Override
    @PostMapping
    public ResponseEntity<Void> createNewUser(@Valid @RequestBody UserForm form) throws UserException {
        UserEntity userEntity = userService.createNewUser(form);
        UserVo userVo = userEntityToVoConverter.convert(userEntity);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(userVo.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<UserVo> getUserById(@PathVariable Long id) throws UserException {
        UserEntity userEntity = userService.getUserById(id);
        UserVo userVo = userEntityToVoConverter.convert(userEntity);
        return ResponseEntity.ok(userVo);
    }

    @Override
    @PostMapping("/preferred-server")
    public ResponseEntity<Void> addServerPreference(ServerPreferenceForm form) throws UserException, ServerException {
        userService.addServerPreference(form);
        return ResponseEntity.ok().build();
    }

}
