package com.dev.notification_service.topic;

public record TopicResponse (Long id, String name){

    public static TopicResponse from(Topic topic){
        return new TopicResponse(topic.getId(), topic.getName());
    }
}
