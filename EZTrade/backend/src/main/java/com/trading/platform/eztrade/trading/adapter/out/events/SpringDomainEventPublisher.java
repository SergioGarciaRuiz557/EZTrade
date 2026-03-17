package com.trading.platform.eztrade.trading.adapter.out.events;

import com.trading.platform.eztrade.trading.application.ports.out.DomainEventPublisherPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida que publica eventos de dominio usando Spring Events.
 * <p>
 * Implementa el puerto {@link DomainEventPublisherPort} para desacoplar la
 * capa de aplicacion de la tecnologia concreta de publicacion.
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisherPort {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructor con el publisher nativo de Spring.
     *
     * @param eventPublisher publicador de eventos del contexto
     */
    public SpringDomainEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publica un evento de dominio en el bus de Spring.
     *
     * @param event evento a publicar
     */
    @Override
    public void publish(Object event) {
        eventPublisher.publishEvent(event);
    }
}
