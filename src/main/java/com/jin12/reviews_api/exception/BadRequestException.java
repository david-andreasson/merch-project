package com.jin12.reviews_api.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
