package com.dev.notification_service.notification;

import jakarta.validation.constraints.NotBlank;

public record PublishRequest(@NotBlank String message) {
}
