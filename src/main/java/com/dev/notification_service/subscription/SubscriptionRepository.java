package com.dev.notification_service.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserId(Long userId);
    List<Subscription> findByTopicId(Long topicId);
    boolean existsByUserIdAndTopicId(Long userId, Long topicId);
    Optional<Subscription> findByUserIdAndTopicId(Long userId, Long topicId);

    @Query("SELECT s FROM Subscription s JOIN FETCH s.user WHERE s.topic.id = :topicId")
    List<Subscription> findByTopicIdWithUsers(@Param("topicId") Long topicId);
}
