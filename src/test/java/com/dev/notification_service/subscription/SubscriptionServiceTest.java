package com.dev.notification_service.subscription;

import com.dev.notification_service.exceptionHandling.ConflictException;
import com.dev.notification_service.exceptionHandling.NotFoundException;
import com.dev.notification_service.topic.Topic;
import com.dev.notification_service.topic.TopicRepository;
import com.dev.notification_service.user.User;
import com.dev.notification_service.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {

    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private UserRepository userRepository;
    @Mock private TopicRepository topicRepository;

    @InjectMocks private SubscriptionService subscriptionService;

    @Test
    void subscribe_whenValid_savesSubscription () {

        // ARRANGE
        User user = User.builder().id(1L).username("bob").build();
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));

        Topic topic = Topic.builder().id(1L).name("tech").build();
        when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));

        when(subscriptionRepository.existsByUserIdAndTopicId(user.getId(), topic.getId())).thenReturn(false);

        Subscription subscription = Subscription.builder().id(1L).user(user).topic(topic).build();
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

        // ACT
        Subscription result = subscriptionService.subscribe(user.getUsername(), topic.getId());


        // ASSERT
        verify(subscriptionRepository).save(any(Subscription.class));
        assertEquals("bob", result.getUser().getUsername());
        assertEquals("tech", result.getTopic().getName());
    }

    @Test
    void subscribe_whenAlreadySubscribed_throwsConflict() {

        // ARRANGE
        User user = User.builder().id(1L).username("bob").build();
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));

        Topic topic = Topic.builder().id(1L).name("tech").build();
        when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));

        when(subscriptionRepository.existsByUserIdAndTopicId(1L, 1L)).thenReturn(true);

        // ACT

        assertThrows(ConflictException.class, () -> subscriptionService.subscribe(user.getUsername(), topic.getId()));

        // ASSERT

        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void subscribe_whenUserNotFound_throwsException() {

        // ARRANGE
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());

        Topic topic = Topic.builder().id(1L).name("tech").build();

        // ACT
        assertThrows(UsernameNotFoundException.class, () -> subscriptionService.subscribe("bob", topic.getId()));

        // ASSERT
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void subscribe_whenTopicNotFound_throwsException() {

        // ARRANGE
        User user = User.builder().id(1L).username("bob").build();
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));
        when(topicRepository.findById(1L)).thenReturn(Optional.empty());

        // ACT

        assertThrows(NotFoundException.class, () -> subscriptionService.subscribe(user.getUsername(), 1L));

        // ASSERT
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }
}
