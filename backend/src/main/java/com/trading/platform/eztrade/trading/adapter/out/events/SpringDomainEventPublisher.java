package com.trading.platform.eztrade.trading.adapter.out.events;

import com.trading.platform.eztrade.trading.application.ports.out.DomainEventPublisherPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Output adapter that publishes domain events using Spring Events.
 * <p>
 * Implements {@link DomainEventPublisherPort} to decouple the application layer
 * from the concrete publication technology.
 */
@Component("tradingDomainEventPublisher")
public class SpringDomainEventPublisher implements DomainEventPublisherPort {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructor with Spring's native publisher.
     *
     * @param eventPublisher context event publisher
     */
    public SpringDomainEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publishes a domain event to the Spring bus.
     *
     * @param event event to publish
     */
    @Override
    public void publish(Object event) {
        eventPublisher.publishEvent(event);
    }
}
