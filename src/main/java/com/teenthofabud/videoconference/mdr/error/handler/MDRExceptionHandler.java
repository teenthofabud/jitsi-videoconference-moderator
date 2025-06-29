package com.teenthofabud.videoconference.mdr.error.handler;

import com.teenthofabud.videoconference.mdr.conference.constants.google.GoogleConferenceErrorType;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceErrorVo;
import com.teenthofabud.videoconference.mdr.error.converter.MDRExceptionToErrorVoConverter;
import com.teenthofabud.videoconference.mdr.error.converter.google.GoogleExceptionToConferenceErrorVoConverter;
import com.teenthofabud.videoconference.mdr.error.core.MDRBaseException;
import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.vo.ErrorVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class MDRExceptionHandler {

    @Autowired
    MDRExceptionToErrorVoConverter mdrExceptionToErrorVoConverter;

    @Autowired
    GoogleExceptionToConferenceErrorVoConverter googleExceptionToConferenceErrorVoConverter;

    @ExceptionHandler(value = { MethodArgumentNotValidException.class })
    public ResponseEntity<ErrorVo> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        String violationMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return e.getParameter().getClass().equals(GoogleConferenceForm.class) ?
                 ResponseEntity.status(200).body(GoogleConferenceErrorVo.builder()
                .error(GoogleConferenceErrorType.TEMPORARY.name())
                .message(violationMessage)
                .build()) : ResponseEntity.status(400).body( ErrorVo.builder()
                .errorCode(MDRErrorCode.MDR_ATTRIBUTE_INVALID.getErrorCode())
                .message(violationMessage)
                .build());
    }

    @ExceptionHandler(value = { GoogleConferenceException.class })
    public ResponseEntity<GoogleConferenceErrorVo> handleGoogleConferenceException(GoogleConferenceException e){
        ResponseEntity<GoogleConferenceErrorVo>  response = parseExceptionToResponse(e);
        log.error(response.getBody().getMessage(),e);
        return response;
    }

    @ExceptionHandler(value = { MDRBaseException.class })
    public ResponseEntity<ErrorVo> fallback(MDRBaseException e){
        ResponseEntity<ErrorVo>  response = parseExceptionToResponse(e);
        log.error(response.getBody().getMessage(),e);
        return response;
    }

    public ResponseEntity<ErrorVo> parseExceptionToResponse(MDRBaseException e) {
        ErrorVo vo = mdrExceptionToErrorVoConverter.convert(e);
        log.debug("{}",vo);
        return ResponseEntity.status(e.getError().getHttpStatusCode()).body(vo);
    }

    public ResponseEntity<GoogleConferenceErrorVo> parseExceptionToResponse(GoogleConferenceException e) {
        GoogleConferenceErrorVo vo = googleExceptionToConferenceErrorVoConverter.convert(e);
        log.debug("{}",vo);
        return ResponseEntity.status(e.getError().getHttpStatusCode()).body(vo);
    }


}
