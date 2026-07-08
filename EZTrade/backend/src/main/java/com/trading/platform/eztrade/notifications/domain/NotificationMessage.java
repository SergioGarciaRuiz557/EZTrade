package com.trading.platform.eztrade.notifications.domain;

import java.time.LocalDateTime;

/**
 * Normalized notification message for all channels.
 * <p>
 * Acts as the module's internal DTO so email, push, websocket, and inbox
 * consume the same structure regardless of the source event.
 *
 * @param recipient recipient identifier (user/email)
 * @param type functional notification type
 * @param title short UI/channel-oriented title
 * @param body descriptive event content
 * @param occurredAt instant when the source event occurred
 */
public record NotificationMessage(
        String recipient,
        NotificationType type,
        String title,
        String body,
        LocalDateTime occurredAt
) {
}

