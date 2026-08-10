package com.dev.notification_service.notification;

import java.time.LocalDateTime;

public record NotificationResponse (Long id, String message, String topicName, LocalDateTime createdAt){

    public static NotificationResponse from(Notification notification){
        return new NotificationResponse(
                notification.getId(),
                notification.getMessage(),
                notification.getTopic().getName(),
                notification.getCreatedAt());
    }
}
