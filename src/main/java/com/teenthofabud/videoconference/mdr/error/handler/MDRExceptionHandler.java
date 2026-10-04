package com.teenthofabud.videoconference.mdr.error.handler;

import com.teenthofabud.videoconference.mdr.conference.google.error.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.google.vo.GoogleConferenceErrorVo;
import com.teenthofabud.videoconference.mdr.error.converter.MDRExceptionToErrorVoConverter;
import com.teenthofabud.videoconference.mdr.error.converter.MethodArgNotValidExceptionToErrorVoConverter;
import com.teenthofabud.videoconference.mdr.conference.google.converter.GoogleExceptionToConferenceErrorVoConverter;
import com.teenthofabud.videoconference.mdr.error.core.MDRBaseException;
import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.vo.ErrorVo;
import jakarta.json.JsonException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.core.JacksonException;

import java.util.List;
import java.util.Objects;

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

    @ExceptionHandler(value = { MissingServletRequestParameterException.class })
    public ResponseEntity<ErrorVo> handleMissingRequestParameterException(MissingServletRequestParameterException e){
        ResponseEntity<ErrorVo> response = parseExceptionToResponse(MDRErrorCode.MDR_ATTRIBUTE_MISSING,
                String.format("Required request parameter %s is missing", e.getParameterName()));
        log.debug(response.getBody().getMessage(),e);
        return response;
    }

    @ExceptionHandler(value = { MethodArgumentTypeMismatchException.class })
    public ResponseEntity<ErrorVo> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e){
        ResponseEntity<ErrorVo> response = parseExceptionToResponse(MDRErrorCode.MDR_ATTRIBUTE_INVALID,
                String.format("%s is not a valid value for %s", e.getValue(), e.getName()));
        log.debug(response.getBody().getMessage(),e);
        return response;
    }

    @ExceptionHandler(value = { HandlerMethodValidationException.class })
    public ResponseEntity<ErrorVo> handleHandlerMethodValidationException(HandlerMethodValidationException e){
        ResponseEntity<ErrorVo> response = parseExceptionToResponse(MDRErrorCode.MDR_ATTRIBUTE_INVALID,
                firstViolationMessage(e));
        log.debug(response.getBody().getMessage(),e);
        return response;
    }

    @ExceptionHandler(value = { HttpMessageNotReadableException.class })
    public ResponseEntity<ErrorVo> handleHttpMessageNotReadableException(HttpMessageNotReadableException e){
        ResponseEntity<ErrorVo> response = parseExceptionToResponse(MDRErrorCode.MDR_ATTRIBUTE_INVALID,
                "Request body could not be read");
        log.debug(response.getBody().getMessage(),e);
        return response;
    }

    @ExceptionHandler(value = { JsonException.class })
    public ResponseEntity<ErrorVo> handleJsonException(JsonException e){
        // a patch document that parses but cannot be applied, e.g. replacing a path that is absent
        ResponseEntity<ErrorVo> response = parseExceptionToResponse(MDRErrorCode.MDR_ATTRIBUTE_INVALID,
                String.format("JSON patch could not be applied : %s", e.getMessage()));
        log.debug(response.getBody().getMessage(),e);
        return response;
    }

    @ExceptionHandler(value = { JacksonException.class })
    public ResponseEntity<ErrorVo> handleJacksonException(JacksonException e){
        // Jackson 3 reports mapping failures as unchecked exceptions; reaching here means a patched
        // document no longer matches the entity it came from
        ResponseEntity<ErrorVo> response = parseExceptionToResponse(MDRErrorCode.MDR_ATTRIBUTE_INVALID,
                String.format("Patched document could not be mapped : %s", e.getOriginalMessage()));
        log.debug(response.getBody().getMessage(),e);
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

    private String firstViolationMessage(HandlerMethodValidationException e) {
        return e.getParameterValidationResults()
                .stream()
                .map(ParameterValidationResult::getResolvableErrors)
                .flatMap(List::stream)
                .map(MessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("One or more request parameters are invalid");
    }

    public ResponseEntity<ErrorVo> parseExceptionToResponse(MDRErrorCode errorCode, String message) {
        ErrorVo vo = ErrorVo.builder()
                .message(message)
                .errorCode(errorCode.getErrorCode())
                .build();
        log.debug("{}",vo);
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(vo);
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
        // google app script will throw and fail on any other http status than 200
        return ResponseEntity.status(200).body(vo);
    }

    public ResponseEntity<ErrorVo> parseExceptionToResponse(MethodArgumentNotValidException e) {
        ErrorVo vo = methodArgNotValidExceptionToErrorVoConverter.convert(e);
        log.debug("{}",vo);
        return ResponseEntity.status(400).body(vo);
    }


}
