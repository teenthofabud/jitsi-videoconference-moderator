package com.teenthofabud.videoconference.mdr.error.converter.google;

import com.teenthofabud.videoconference.mdr.conference.constants.google.GoogleConferenceErrorType;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceErrorVo;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

@Component
public class GoogleExceptionToConferenceErrorVoConverter implements Converter<GoogleConferenceException, GoogleConferenceErrorVo> {
    @Override
    public GoogleConferenceErrorVo convert(GoogleConferenceException source) {
        String msg = source.getErrorType().getMessage();
        if (source.getParameters() != null) {
            Deque<Object> parameters = new ArrayDeque(Arrays.asList(source.getParameters()));
            msg = String.format(msg, parameters.toArray(new Object[parameters.size()]));
        }
        if(source.getErrorType().equals(GoogleConferenceErrorType.AUTHENTICATION))
            return GoogleConferenceErrorVo.builder()
                    .error(source.getErrorType().name())
                    .authenticationUrl("auth url here")
                    .message(msg)
                    .build();
        else
            return GoogleConferenceErrorVo.builder()
                    .error(source.getErrorType().name())
                    .authenticationUrl(null)
                    .message(msg)
                    .build();

    }
}
