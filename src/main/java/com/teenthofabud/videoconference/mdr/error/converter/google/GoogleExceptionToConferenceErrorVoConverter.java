package com.teenthofabud.videoconference.mdr.error.converter.google;

import com.teenthofabud.videoconference.mdr.conference.constants.google.GoogleConferenceErrorType;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceErrorVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Locale;

@Component
public class GoogleExceptionToConferenceErrorVoConverter implements Converter<GoogleConferenceException, GoogleConferenceErrorVo> {

    @Override
    public GoogleConferenceErrorVo convert(GoogleConferenceException source) {

       return source.getErrorType().equals(GoogleConferenceErrorType.AUTHENTICATION) ?
              GoogleConferenceErrorVo.builder()
                    .error(source.getErrorType().name())
                    .authenticationUrl("auth url here")
                    .message(source.getMessage())
                    .build() :
               GoogleConferenceErrorVo.builder()
                    .error(source.getErrorType().name())
                    .authenticationUrl(null)
                    .message(source.getMessage())
                    .build();

    }
}
