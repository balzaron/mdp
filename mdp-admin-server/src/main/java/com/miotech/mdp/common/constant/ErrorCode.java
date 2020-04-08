package com.miotech.mdp.common.constant;

public enum ErrorCode {

    FAILED(1, "Operation failed"),
    NOT_FOUND(404, "Not Found");


    private final int code;
    private final String message;
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
