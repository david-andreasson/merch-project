package com.jin12.reviews_api.exception;

public class ApiKeyUpdateException extends RuntimeException {
    public ApiKeyUpdateException(String message) {
        super(message);
    }

    public ApiKeyUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}