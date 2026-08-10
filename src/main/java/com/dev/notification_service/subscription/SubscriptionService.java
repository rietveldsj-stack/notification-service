package com.dev.notification_service.subscription;

import com.dev.notification_service.exceptionHandling.ConflictException;
import com.dev.notification_service.exceptionHandling.NotFoundException;
import com.dev.notification_service.topic.Topic;
import com.dev.notification_service.topic.TopicRepository;
import com.dev.notification_service.user.User;
import com.dev.notification_service.user.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, UserRepository userRepository, TopicRepository topicRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
    }

    public Subscription subscribe(String userName, Long topicId){

        User user = userRepository.findByUsername(userName).orElseThrow( ()-> new UsernameNotFoundException("User not found"));

        Topic topic = topicRepository.findById(topicId).orElseThrow( () -> new NotFoundException("Topic not found"));

        if (subscriptionRepository.existsByUserIdAndTopicId(user.getId(), topicId)){
            throw new ConflictException("You are already subscribed to this topic");
        }
        Subscription subscription = Subscription.builder()
                .user(user)
                .topic(topic)
                .build();

        return subscriptionRepository.save(subscription);
    }

    public List<Subscription> getSubscriptions (String username) {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return subscriptionRepository.findByUserId(user.getId());
    }

    public void unsubscribe(String username, Long topicId) {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Subscription subscription = subscriptionRepository.findByUserIdAndTopicId(user.getId(), topicId).orElseThrow(
                () -> new NotFoundException("Subscription not found"));

        subscriptionRepository.delete(subscription);
    }
}
