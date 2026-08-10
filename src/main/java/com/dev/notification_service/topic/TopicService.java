package com.dev.notification_service.topic;

import com.dev.notification_service.exceptionHandling.ConflictException;
import org.springframework.stereotype.Service;

@Service
public class TopicService {

    private final TopicRepository topicRepository;

    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    public TopicResponse createTopic (String name){

        if(topicRepository.existsByName(name)){
            throw new ConflictException("Topic already exists");
        }

        Topic topic = Topic.builder()
                    .name(name)
                    .build();

        return TopicResponse.from(topicRepository.save(topic));
    }
}
