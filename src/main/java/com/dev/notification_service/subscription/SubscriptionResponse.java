package com.dev.notification_service.subscription;

import java.time.LocalDateTime;

public record SubscriptionResponse(Long id, String topicName, LocalDateTime subscribedAt) {

    public static SubscriptionResponse from (Subscription subscription){
        return new SubscriptionResponse(subscription.getId(), subscription.getTopic().getName(), subscription.getCreatedAt());
    }
}
