package com.trading.platform.eztrade.portfolio.adapter.out.events;

import com.trading.platform.eztrade.portfolio.application.ports.out.DomainEventPublisherPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component("portfolioDomainEventPublisher")
public class SpringDomainEventPublisher implements DomainEventPublisherPort {

    private final ApplicationEventPublisher eventPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(Object event) {
        eventPublisher.publishEvent(event);
    }
}

