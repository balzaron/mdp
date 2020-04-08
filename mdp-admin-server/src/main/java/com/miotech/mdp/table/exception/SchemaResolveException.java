package com.miotech.mdp.table.exception;

public class SchemaResolveException extends RuntimeException {
    public SchemaResolveException(String message) {
        super(message);
    }

    public SchemaResolveException(Throwable cause) {
        super(cause);
    }
}
