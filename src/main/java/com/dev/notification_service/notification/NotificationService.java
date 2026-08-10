package com.dev.notification_service.notification;

import com.dev.notification_service.exceptionHandling.NotFoundException;
import com.dev.notification_service.subscription.Subscription;
import com.dev.notification_service.subscription.SubscriptionRepository;
import com.dev.notification_service.topic.Topic;
import com.dev.notification_service.topic.TopicRepository;
import com.dev.notification_service.user.User;
import com.dev.notification_service.user.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final TopicRepository topicRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SseService sseService;

    public NotificationService(NotificationRepository notificationRepository, TopicRepository topicRepository,
                               SubscriptionRepository subscriptionRepository, UserRepository userRepository,
                               SseService sseService) {
        this.notificationRepository = notificationRepository;
        this.topicRepository = topicRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.sseService = sseService;
    }

    @Transactional
    public void publish (Long topicId, String message) {

        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new NotFoundException("Topic not found"));

        List<Subscription> subscriptions = subscriptionRepository.findByTopicIdWithUsers(topicId);

        List<Notification> notifications = subscriptions.stream()
                .map(sub -> Notification.builder()
                        .user(sub.getUser())
                        .topic(topic)
                        .message(message)
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
        notifications.forEach(n ->
                sseService.sendToUser(n.getUser().getId(), NotificationResponse.from(n)));
    }

    public SseEmitter streamForUser(String username){

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return sseService.subscribe(user.getId());
    }
}
