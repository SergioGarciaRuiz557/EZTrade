package com.trading.platform.eztrade.trading.application.ports.out;

/**
 * Puerto de salida para publicacion de eventos de dominio.
 * <p>
 * Permite que la capa de aplicacion emita eventos sin depender de una
 * tecnologia concreta de mensajeria/eventos.
 */
public interface DomainEventPublisherPort {

    /**
     * Publica un evento de dominio.
     *
     * @param event evento a publicar
     */
    void publish(Object event);
}
