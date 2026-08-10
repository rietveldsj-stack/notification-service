package com.dev.notification_service.exceptionHandling;

public class NotFoundException extends RuntimeException{
    public NotFoundException(String message){
        super(message);
    }
}
