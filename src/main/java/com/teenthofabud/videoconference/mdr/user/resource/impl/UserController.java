package com.teenthofabud.videoconference.mdr.user.resource.impl;

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

    @Override
    @PostMapping
    public ResponseEntity<Void> createNewUser(@Valid @RequestBody UserForm form) throws UserException {
        UserVo userVo = userService.createNewUser(form);
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
        UserVo userVo = userService.getUserById(id);
        return ResponseEntity.ok(userVo);
    }
}
