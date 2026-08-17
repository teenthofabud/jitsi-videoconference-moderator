package com.teenthofabud.videoconference.mdr.error.converter;

import com.teenthofabud.videoconference.mdr.conference.constants.google.GoogleConferenceErrorType;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceErrorVo;
import com.teenthofabud.videoconference.mdr.error.core.MDRBaseException;
import com.teenthofabud.videoconference.mdr.error.core.MDRMessageSource;
import com.teenthofabud.videoconference.mdr.vo.ErrorVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Locale;

@Component
public class MDRExceptionToErrorVoConverter implements Converter<MDRBaseException, ErrorVo> {

    @Autowired
    private MDRMessageSource messageSource;

    @Override
    public ErrorVo convert(MDRBaseException source) {

        String msg = this.messageSource.getMessage(source.getError().getErrorCode(), null, Locale.getDefault());
        if (source.getParameters() != null) {
            Deque<Object> parameters = new ArrayDeque(Arrays.asList(source.getParameters()));
            msg = String.format(msg, parameters.toArray(new Object[parameters.size()]));
        }

        return ErrorVo.builder()
                .errorCode(source.getError().getErrorCode())
                .message(msg)
                .build();
    }


}