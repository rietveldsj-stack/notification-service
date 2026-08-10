package com.dev.notification_service.subscription;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/{topicId}")
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse subscribe(
            @PathVariable Long topicId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Subscription subscription = subscriptionService.subscribe(userDetails.getUsername(), topicId);

        return SubscriptionResponse.from(subscription);
    }

    @GetMapping
    public List<SubscriptionResponse> getSubscriptions(@AuthenticationPrincipal UserDetails userDetails) {

         return subscriptionService.getSubscriptions(userDetails.getUsername()).stream()
                 .map(SubscriptionResponse::from)
                 .toList();
    }
}
