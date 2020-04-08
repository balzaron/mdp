package com.miotech.mdp.quality.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RequestAttributeErrorException extends RuntimeException {
    public RequestAttributeErrorException(String message) {
        super(message);
    }

    public RequestAttributeErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
