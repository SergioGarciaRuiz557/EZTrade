package com.trading.platform.eztrade.portfolio.adapter.out.events;

import com.trading.platform.eztrade.portfolio.application.ports.out.DomainEventPublisherPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida que publica eventos de portfolio mediante Spring Events.
 * <p>
 * El nombre del bean evita ambiguedades con otros publicadores equivalentes de
 * modulos como trading o wallet.
 */
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

