package com.teenthofabud.videoconference.mdr.error.handler;

import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceErrorVo;
import com.teenthofabud.videoconference.mdr.error.converter.MDRExceptionToErrorVoConverter;
import com.teenthofabud.videoconference.mdr.error.converter.MethodArgNotValidExceptionToErrorVoConverter;
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
    @Autowired
    MethodArgNotValidExceptionToErrorVoConverter methodArgNotValidExceptionToErrorVoConverter;

    @ExceptionHandler(value = { MethodArgumentNotValidException.class })
    public ResponseEntity<ErrorVo> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        ResponseEntity<ErrorVo> response = parseExceptionToResponse(e);
        log.debug(response.getBody().getMessage(),e);
        return response;
    }

    @ExceptionHandler(value = { GoogleConferenceException.class })
    public ResponseEntity<GoogleConferenceErrorVo> handleGoogleConferenceException(GoogleConferenceException e){
        ResponseEntity<GoogleConferenceErrorVo>  response = parseExceptionToResponse(e);
        log.error(response.getBody().getMessage(),e);
        return response;
    }

    @ExceptionHandler(value = { MDRBaseException.class })
    public ResponseEntity<ErrorVo> handleMdrExceptions(MDRBaseException e){
        ResponseEntity<ErrorVo> response = parseExceptionToResponse(e);
        log.debug(response.getBody().getMessage(),e);
        return response;
    }

    @ExceptionHandler(value = { Exception.class })
    public ResponseEntity<ErrorVo> fallback(Exception e){
        ResponseEntity<ErrorVo> response = parseExceptionToResponse(e);
        log.debug(response.getBody().getMessage(),e);
        return response;
    }

    public ResponseEntity<ErrorVo> parseExceptionToResponse(Exception e) {
        ErrorVo vo = ErrorVo.builder()
                        .message(String.format("Something unexpected happened : %s",e.getMessage()))
                                .errorCode(MDRErrorCode.MDR_ACTION_FAILURE.getErrorCode()).build();
        log.debug("{}",vo);
        log.debug("",e);
        return ResponseEntity.status(500).body(vo);
    }

    public ResponseEntity<ErrorVo> parseExceptionToResponse(MDRBaseException e){
        ErrorVo vo = mdrExceptionToErrorVoConverter.convert(e);
        log.debug("{}",vo);
        return ResponseEntity.status(e.getError().getHttpStatusCode()).body(vo);
    }

    public ResponseEntity<GoogleConferenceErrorVo> parseExceptionToResponse(GoogleConferenceException e) {
        GoogleConferenceErrorVo vo = googleExceptionToConferenceErrorVoConverter.convert(e);
        log.debug("{}",vo);
        return ResponseEntity.status(e.getError().getHttpStatusCode()).body(vo);
    }

    public ResponseEntity<ErrorVo> parseExceptionToResponse(MethodArgumentNotValidException e) {
        ErrorVo vo = methodArgNotValidExceptionToErrorVoConverter.convert(e);
        log.debug("{}",vo);
        return ResponseEntity.status(400).body(vo);
    }


}
