package com.trading.platform.eztrade.notifications.adapter.in.events;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration that enables asynchronous event listeners in notifications.
 * <p>
 * The module can process notices without blocking the main flow that published
 * the event, provided the listeners are annotated as asynchronous.
 */
@Configuration
@EnableAsync
class AsyncDomainEventsConfig {
}
