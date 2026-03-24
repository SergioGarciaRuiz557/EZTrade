package com.trading.platform.eztrade.notifications.application.ports.out;

import com.trading.platform.eztrade.notifications.domain.NotificationMessage;

public interface WebSocketNotificationPort {

    void send(NotificationMessage message);
}

