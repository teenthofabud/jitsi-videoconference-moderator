package com.teenthofabud.videoconference.mdr.error.converter;

import com.teenthofabud.videoconference.mdr.conference.constants.google.GoogleConferenceErrorType;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceErrorVo;
import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.vo.ErrorVo;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Component
public class MethodArgNotValidExceptionToErrorVoConverter implements Converter<MethodArgumentNotValidException, ErrorVo> {
    @Override
    public ErrorVo convert(MethodArgumentNotValidException e) {
        String violationMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return e.getParameter().getClass().equals(GoogleConferenceForm.class) ?
               GoogleConferenceErrorVo.builder()
                        .error(GoogleConferenceErrorType.TEMPORARY.name())
                        .message(violationMessage)
                        .build() : ErrorVo.builder()
                .errorCode(MDRErrorCode.MDR_ATTRIBUTE_INVALID.getErrorCode())
                .message(violationMessage)
                .build();
    }
}
