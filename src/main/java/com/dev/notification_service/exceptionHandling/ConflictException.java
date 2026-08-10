package com.dev.notification_service.exceptionHandling;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
