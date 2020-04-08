package com.miotech.mdp.quality.exception;

/**
 * @author: shanyue.gao
 * @date: 2020/2/26 4:50 PM
 */
public class DividedZeroException extends RuntimeException{

    public DividedZeroException(String message) {
        super(message);
    }

    public DividedZeroException(String message, Throwable cause) {
        super(message, cause);
    }
}
