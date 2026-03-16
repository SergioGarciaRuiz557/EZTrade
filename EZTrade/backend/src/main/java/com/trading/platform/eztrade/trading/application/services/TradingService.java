package com.trading.platform.eztrade.trading.application.services;

import com.trading.platform.eztrade.trading.application.ports.in.CancelOrderUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.ExecuteOrderUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.GetOrdersUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.PlaceOrderUseCase;
import com.trading.platform.eztrade.trading.application.ports.out.DomainEventPublisherPort;
import com.trading.platform.eztrade.trading.application.ports.out.TradeOrderRepositoryPort;
import com.trading.platform.eztrade.trading.domain.Money;
import com.trading.platform.eztrade.trading.domain.OrderId;
import com.trading.platform.eztrade.trading.domain.Quantity;
import com.trading.platform.eztrade.trading.domain.TradeOrder;
import com.trading.platform.eztrade.trading.domain.TradingDomainException;
import com.trading.platform.eztrade.trading.domain.events.OrderCancelledEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderPlacedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de aplicacion del modulo trading.
 * <p>
 * Implementa los casos de uso principales del contexto:
 * alta, ejecucion, cancelacion y consulta de ordenes.
 * <p>
 * Rol arquitectonico:
 * <ul>
 *   <li>Orquesta el dominio.</li>
 *   <li>Persistencia via {@link TradeOrderRepositoryPort}.</li>
 *   <li>Comunicacion inter-modulo via {@link DomainEventPublisherPort}.</li>
 * </ul>
 */
@Service
@Transactional
public class TradingService implements PlaceOrderUseCase, ExecuteOrderUseCase, CancelOrderUseCase, GetOrdersUseCase {

    private final TradeOrderRepositoryPort tradeOrderRepositoryPort;
    private final DomainEventPublisherPort domainEventPublisherPort;

    /**
     * Crea el servicio de aplicacion con sus puertos de salida.
     *
     * @param tradeOrderRepositoryPort puerto de repositorio de ordenes
     * @param domainEventPublisherPort puerto para publicacion de eventos
     */
    public TradingService(TradeOrderRepositoryPort tradeOrderRepositoryPort,
                          DomainEventPublisherPort domainEventPublisherPort) {
        this.tradeOrderRepositoryPort = tradeOrderRepositoryPort;
        this.domainEventPublisherPort = domainEventPublisherPort;
    }

    /**
     * Registra una nueva orden y publica {@link OrderPlacedEvent}.
     *
     * @param command datos de creacion de la orden
     * @return orden creada y persistida
     */
    @Override
    public TradeOrder place(PlaceOrderCommand command) {
        TradeOrder order = TradeOrder.place(
                command.owner(),
                command.symbol(),
                command.side(),
                new Quantity(command.quantity()),
                new Money(command.price())
        );

        TradeOrder saved = tradeOrderRepositoryPort.save(order);

        domainEventPublisherPort.publish(new OrderPlacedEvent(
                saved.id(),
                saved.owner(),
                saved.symbol(),
                saved.side(),
                saved.quantity().value(),
                saved.price().value(),
                LocalDateTime.now()
        ));

        return saved;
    }

    /**
     * Ejecuta una orden existente y publica {@link OrderExecutedEvent}.
     *
     * @param orderId identificador de la orden
     * @return orden ejecutada
     * @throws TradingDomainException si la orden no existe o no puede ejecutarse
     */
    @Override
    public TradeOrder execute(OrderId orderId) {
        TradeOrder current = tradeOrderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new TradingDomainException("Order not found: " + orderId.value()));

        TradeOrder executed = tradeOrderRepositoryPort.save(current.execute());

        domainEventPublisherPort.publish(new OrderExecutedEvent(
                executed.id().value(),
                executed.owner(),
                executed.symbol(),
                executed.side().name(),
                executed.quantity().value(),
                executed.price().value(),
                LocalDateTime.now()
        ));

        return executed;
    }

    /**
     * Cancela una orden existente y publica {@link OrderCancelledEvent}.
     *
     * @param orderId identificador de la orden
     * @param requestedBy usuario que solicita la cancelacion
     * @return orden cancelada
     * @throws TradingDomainException si la orden no existe o no puede cancelarse
     */
    @Override
    public TradeOrder cancel(OrderId orderId, String requestedBy) {
        TradeOrder current = tradeOrderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new TradingDomainException("Order not found: " + orderId.value()));

        TradeOrder cancelled = tradeOrderRepositoryPort.save(current.cancel(requestedBy));

        domainEventPublisherPort.publish(new OrderCancelledEvent(
                cancelled.id(),
                cancelled.owner(),
                cancelled.symbol(),
                LocalDateTime.now()
        ));

        return cancelled;
    }

    /**
     * Consulta una orden por id.
     *
     * @param orderId identificador de la orden
     * @return orden encontrada
     * @throws TradingDomainException si no existe
     */
    @Override
    @Transactional(readOnly = true)
    public TradeOrder getById(OrderId orderId) {
        return tradeOrderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new TradingDomainException("Order not found: " + orderId.value()));
    }

    /**
     * Consulta todas las ordenes de un propietario.
     *
     * @param owner propietario de las ordenes
     * @return lista de ordenes
     */
    @Override
    @Transactional(readOnly = true)
    public List<TradeOrder> getByOwner(String owner) {
        return tradeOrderRepositoryPort.findByOwner(owner);
    }
}

