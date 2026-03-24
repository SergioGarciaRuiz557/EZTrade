package com.trading.platform.eztrade.notifications.adapter.out.inbox.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataInboxNotificationRepository extends JpaRepository<InboxNotificationJpaEntity, Long> {
}

