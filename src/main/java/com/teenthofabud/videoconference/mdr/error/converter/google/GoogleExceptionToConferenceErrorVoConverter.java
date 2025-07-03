package com.teenthofabud.videoconference.mdr.error.converter.google;

import com.teenthofabud.videoconference.mdr.conference.constants.google.GoogleConferenceErrorType;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceErrorVo;
import com.teenthofabud.videoconference.mdr.error.core.MDRMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Locale;

@Component
public class GoogleExceptionToConferenceErrorVoConverter implements Converter<GoogleConferenceException, GoogleConferenceErrorVo> {

    @Autowired
    private MDRMessageSource messageSource;

    @Override
    public GoogleConferenceErrorVo convert(GoogleConferenceException source) {
        String msg = this.messageSource.getMessage(source.getError().getErrorCode(), null, Locale.getDefault());
        if (source.getParameters() != null) {
            Deque<Object> parameters = new ArrayDeque(Arrays.asList(source.getParameters()));
            msg = String.format(msg, parameters.toArray(new Object[parameters.size()]));
        }
       return source.getErrorType().equals(GoogleConferenceErrorType.AUTHENTICATION) ?
              GoogleConferenceErrorVo.builder()
                    .error(source.getErrorType().name())
                    .authenticationUrl("auth url here")
                    .message(msg)
                    .build() :
               GoogleConferenceErrorVo.builder()
                    .error(source.getErrorType().name())
                    .authenticationUrl(null)
                    .message(msg)
                    .build();

    }
}
