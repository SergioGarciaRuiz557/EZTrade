package com.trading.platform.eztrade.notifications.adapter.in.events;

import com.trading.platform.eztrade.notifications.application.ports.in.NotifyOnDomainEventsUseCase;
import com.trading.platform.eztrade.portfolio.domain.events.PortfolioValuationUpdatedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderCancelledEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderPlacedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada del modulo notifications basado en eventos Spring.
 * <p>
 * Su unica responsabilidad es traducir eventos publicados dentro del proceso
 * a invocaciones del caso de uso de aplicacion, manteniendo separado el transporte
 * (Spring Events) de la logica de negocio de notificaciones.
 */
@Component
public class DomainEventsListener {

    private final NotifyOnDomainEventsUseCase notifyOnDomainEventsUseCase;

    public DomainEventsListener(NotifyOnDomainEventsUseCase notifyOnDomainEventsUseCase) {
        this.notifyOnDomainEventsUseCase = notifyOnDomainEventsUseCase;
    }

    /**
     * Reacciona cuando una orden se registra y delega su procesamiento.
     *
     * @param event evento emitido por trading al crear una orden
     */
    @EventListener
    public void on(OrderPlacedEvent event) {
        notifyOnDomainEventsUseCase.handle(event);
    }

    /**
     * Reacciona cuando una orden se ejecuta y delega su procesamiento.
     *
     * @param event evento emitido por trading al ejecutar una orden
     */
    @EventListener
    public void on(OrderExecutedEvent event) {
        notifyOnDomainEventsUseCase.handle(event);
    }

    /**
     * Reacciona cuando una orden se cancela y delega su procesamiento.
     *
     * @param event evento emitido por trading al cancelar una orden
     */
    @EventListener
    public void on(OrderCancelledEvent event) {
        notifyOnDomainEventsUseCase.handle(event);
    }

    /**
     * Reacciona cuando portfolio publica una nueva valoracion agregada.
     *
     * @param event evento de portfolio con datos de cash/coste/pnl
     */
    @EventListener
    public void on(PortfolioValuationUpdatedEvent event) {
        notifyOnDomainEventsUseCase.handle(event);
    }
}

