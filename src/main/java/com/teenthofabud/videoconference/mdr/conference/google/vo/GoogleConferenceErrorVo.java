package com.teenthofabud.videoconference.mdr.conference.google.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.teenthofabud.videoconference.mdr.vo.ErrorVo;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuperBuilder
public class GoogleConferenceErrorVo extends ErrorVo {

    String authenticationUrl;
    String error;

    public GoogleConferenceErrorVo(String authenticationUrl, String error, String message) {
        super(message,error);
        this.authenticationUrl = authenticationUrl;
        this.error = error;
    }

    public GoogleConferenceErrorVo(String error, String message) {
        super(message,error);
        this.error = error;
    }


}
