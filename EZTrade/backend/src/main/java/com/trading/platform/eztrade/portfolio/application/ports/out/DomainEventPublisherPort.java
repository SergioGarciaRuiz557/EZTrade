package com.trading.platform.eztrade.portfolio.application.ports.out;

/**
 * Puerto de salida para publicar eventos generados por portfolio.
 * <p>
 * Evita que la capa de aplicacion dependa directamente de Spring Events.
 */
public interface DomainEventPublisherPort {

    /** Publica un evento de dominio o integracion dentro del proceso Spring. */
    void publish(Object event);
}

