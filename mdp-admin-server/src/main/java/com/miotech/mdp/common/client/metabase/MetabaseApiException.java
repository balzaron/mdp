package com.miotech.mdp.common.client.metabase;

public class MetabaseApiException extends RuntimeException {
    public MetabaseApiException(String message) {
        super(message);
    }

    public MetabaseApiException(Throwable cause) {
        super(cause);
    }
}
