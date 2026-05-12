package com.trading.platform.eztrade.notifications.adapter.in.events;

import com.trading.platform.eztrade.notifications.application.ports.in.NotifyOnDomainEventsUseCase;
import com.trading.platform.eztrade.portfolio.domain.events.PortfolioValuationUpdatedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderCancelledEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderPlacedEvent;
import com.trading.platform.eztrade.wallet.domain.events.InsufficientFundsEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(OrderPlacedEvent event) {
        notifyOnDomainEventsUseCase.handle(event);
    }

    /**
     * Reacciona cuando una orden se ejecuta y delega su procesamiento.
     *
     * @param event evento emitido por trading al ejecutar una orden
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(OrderExecutedEvent event) {
        notifyOnDomainEventsUseCase.handle(event);
    }

    /**
     * Reacciona cuando una orden se cancela y delega su procesamiento.
     *
     * @param event evento emitido por trading al cancelar una orden
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(OrderCancelledEvent event) {
        notifyOnDomainEventsUseCase.handle(event);
    }

    /**
     * Reacciona cuando wallet reporta fondos insuficientes.
     *
     * @param event evento de fondos insuficientes
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(InsufficientFundsEvent event) {
        notifyOnDomainEventsUseCase.handle(event);
    }

    /**
     * Reacciona cuando portfolio publica una nueva valoracion agregada.
     *
     * @param event evento de portfolio con datos de cash/coste/pnl
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(PortfolioValuationUpdatedEvent event) {
        notifyOnDomainEventsUseCase.handle(event);
    }
}

