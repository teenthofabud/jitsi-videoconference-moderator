package com.teenthofabud.videoconference.mdr.core.util;

import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Component
public class UserIdHasher {
    public String hash(String userId){
        return DigestUtils.md5DigestAsHex(userId.getBytes(StandardCharsets.UTF_8));
    }
}
