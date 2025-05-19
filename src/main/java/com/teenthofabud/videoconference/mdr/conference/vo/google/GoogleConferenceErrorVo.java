package com.teenthofabud.videoconference.mdr.conference.vo.google;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@Builder
public class GoogleConferenceErrorVo {

    String authenticationUrl;
    String error;
    String message;

}
