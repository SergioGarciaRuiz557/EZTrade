package com.trading.platform.eztrade.wallet.application.ports.out;

/**
 * Output port for publishing domain events.
 * <p>
 * Modeled as an interface to decouple the application from the concrete
 * publication mechanism (Spring events, messaging, etc.).
 */
public interface DomainEventPublisherPort {

    /** Publishes an event (usually a record) so other components can consume it. */
    void publish(Object event);
}

