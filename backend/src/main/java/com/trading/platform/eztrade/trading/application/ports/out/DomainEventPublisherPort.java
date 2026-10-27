package com.trading.platform.eztrade.trading.application.ports.out;

/**
 * Output port for publishing domain events.
 * <p>
 * Allows the application layer to emit events without depending on a concrete
 * messaging/events technology.
 */
public interface DomainEventPublisherPort {

    /**
     * Publishes a domain event.
     *
     * @param event event to publish
     */
    void publish(Object event);
}
