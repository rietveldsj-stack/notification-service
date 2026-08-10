package com.dev.notification_service.notification;

import com.dev.notification_service.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    public NotificationController(NotificationService notificationService, JwtUtil jwtUtil) {
        this.notificationService = notificationService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/api/topics/{topicId}/notifications")
    @PreAuthorize("hasRole('ADMIN')")
    public void publish (@Valid @RequestBody PublishRequest request,
                                 @PathVariable Long topicId) {

        notificationService.publish(topicId, request.message());
    }

    @GetMapping("/api/notifications/stream")
    public SseEmitter stream (@RequestParam String token){

        String username = jwtUtil.extractUsername(token);
        return notificationService.streamForUser(username);
    }
}
