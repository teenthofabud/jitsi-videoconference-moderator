package com.teenthofabud.videoconference.mdr.error.core;

public enum MDRErrorCode implements MDRError {

    MDR_ATTRIBUTE_INVALID("MDR-002", 400), // syntactic
    MDR_NOT_FOUND("MDR-006", 404),
    MDR_ATTRIBUTE_UNEXPECTED("MDR-007", 422), // semantic
    MDR_EXISTS("MDR-005", 409),
    MDR_INACTIVE("MDR-008", 400),
    MDR_ACTION_FAILURE("MDR-001", 500),
    MDR_DUPLICATE_ATTRIBUTES("MDR-009", 409),
    MDR_ACTION_REPEATED("MDR-010", 409),
    MDR_ATTRIBUTE_MISSING("MDR-004",400 );

    private String errorCode;
    private int httpStatusCode;


    MDRErrorCode(String errorCode, int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
    }


    @Override
    public String toString() {
        return "MDRErrorCode %s -> errorCode=%s, httpStatusCode=%s".formatted(this.name(),errorCode,httpStatusCode);
    }

    @Override
    public String getErrorCode() {
        return this.errorCode;
    }

    @Override
    public Integer getHttpStatusCode() {
        return this.httpStatusCode;
    }



}