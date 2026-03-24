package com.trading.platform.eztrade.wallet.adapter.out.events;

import com.trading.platform.eztrade.wallet.application.ports.out.DomainEventPublisherPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
/**
 * Adaptador de salida que implementa {@link DomainEventPublisherPort} usando el bus de eventos de Spring.
 * <p>
 * Convierte la intención de "publicar un evento de dominio" en una llamada a
 * {@link ApplicationEventPublisher#publishEvent(Object)}.
 */
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

