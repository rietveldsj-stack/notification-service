package com.dev.notification_service.topic;

import com.dev.notification_service.exceptionHandling.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @InjectMocks
    private TopicService topicService;

    @Test
    void createTopic_whenNameIsAvailable_savesAndReturnsResponse() {
        // ARRANGE
        when(topicRepository.existsByName("tech")).thenReturn(false);
        Topic saved = Topic.builder().id(1L).name("tech").build();
        when(topicRepository.save(any(Topic.class))).thenReturn(saved);

        // ACT
        TopicResponse response = topicService.createTopic("tech");

        // ASSERT
        assertEquals("tech", response.name());
        assertEquals(1L, response.id());
        verify(topicRepository).save(any(Topic.class));
    }

    @Test
    void createTopic_whenNameExists_throwsConflict() {

        // ARRANGE
        when(topicRepository.existsByName("tech")).thenReturn(true);

        // ACT
        assertThrows(ConflictException.class, () -> topicService.createTopic("tech"));

        // ASSERT
        verify(topicRepository, never()).save(any(Topic.class));
    }
}