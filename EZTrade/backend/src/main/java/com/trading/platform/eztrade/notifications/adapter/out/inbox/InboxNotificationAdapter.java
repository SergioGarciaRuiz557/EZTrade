package com.trading.platform.eztrade.notifications.adapter.out.inbox;

import com.trading.platform.eztrade.notifications.adapter.out.inbox.jpa.InboxNotificationJpaEntity;
import com.trading.platform.eztrade.notifications.adapter.out.inbox.jpa.SpringDataInboxNotificationRepository;
import com.trading.platform.eztrade.notifications.application.ports.out.InboxNotificationPort;
import com.trading.platform.eztrade.notifications.domain.NotificationMessage;
import org.springframework.stereotype.Repository;

/**
 * Persistencia de notificaciones in-app (inbox).
 */
@Repository
public class InboxNotificationAdapter implements InboxNotificationPort {

    private final SpringDataInboxNotificationRepository repository;

    public InboxNotificationAdapter(SpringDataInboxNotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(NotificationMessage message) {
        InboxNotificationJpaEntity entity = new InboxNotificationJpaEntity();
        entity.setRecipient(message.recipient());
        entity.setType(message.type().name());
        entity.setTitle(message.title());
        entity.setBody(message.body());
        entity.setOccurredAt(message.occurredAt());
        entity.setRead(false);
        repository.save(entity);
    }
}

