package com.trading.platform.eztrade.wallet.application.ports.out;

/**
 * Puerto de salida para publicar eventos de dominio.
 * <p>
 * Se modela como interfaz para desacoplar la aplicación del mecanismo concreto de publicación (Spring events,
 * mensajería, etc.).
 */
public interface DomainEventPublisherPort {

    /** Publica un evento (normalmente un record) para que otros componentes lo consuman. */
    void publish(Object event);
}

