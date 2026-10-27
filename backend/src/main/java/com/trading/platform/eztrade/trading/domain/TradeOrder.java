package com.trading.platform.eztrade.trading.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Root aggregate of the trading module.
 * <p>
 * Models a buy/sell order and centralizes its invariants: valid owner, valid
 * symbol, positive quantities/prices, and allowed state transitions.
 * <p>
 * This class belongs to the pure domain and does not depend on Spring or JPA.
 */
public class TradeOrder {

    private final OrderId id;
    private final String owner;
    private final String symbol;
    private final OrderSide side;
    private final Quantity quantity;
    private final Money price;
    private final OrderStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime executedAt;

    private TradeOrder(OrderId id,
                       String owner,
                       String symbol,
                       OrderSide side,
                       Quantity quantity,
                       Money price,
                       OrderStatus status,
                       LocalDateTime createdAt,
                       LocalDateTime executedAt) {
        this.id = id;
        this.owner = validateOwner(owner);
        this.symbol = validateSymbol(symbol);
        this.side = Objects.requireNonNull(side, "El tipo de orden es obligatorio");
        this.quantity = Objects.requireNonNull(quantity, "La cantidad es obligatoria");
        this.price = Objects.requireNonNull(price, "El precio es obligatorio");
        this.status = Objects.requireNonNull(status, "El estado de la orden es obligatorio");
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creacion es obligatoria");
        this.executedAt = executedAt;
    }

    /**
     * Factory for creating a new order in {@link OrderStatus#PENDING} state.
     *
     * @param owner order owner's email/identifier
     * @param symbol asset symbol
     * @param side order side (buy or sell)
     * @param quantity requested quantity
     * @param price unit price
     * @return new pending order
     */
    public static TradeOrder place(String owner, String symbol, OrderSide side, Quantity quantity, Money price) {
        return new TradeOrder(
                null,
                owner,
                symbol,
                side,
                quantity,
                price,
                OrderStatus.PENDING,
                LocalDateTime.now(),
                null
        );
    }

    /**
     * Factory for reconstructing the aggregate from persistence.
     *
     * @param id order id
     * @param owner owner
     * @param symbol symbol
     * @param side order side
     * @param quantity quantity
     * @param price unit price
     * @param status current status
     * @param createdAt creation date
     * @param executedAt execution date (can be null)
     * @return rehydrated aggregate
     */
    public static TradeOrder rehydrate(OrderId id,
                                       String owner,
                                       String symbol,
                                       OrderSide side,
                                       Quantity quantity,
                                       Money price,
                                       OrderStatus status,
                                       LocalDateTime createdAt,
                                       LocalDateTime executedAt) {
        return new TradeOrder(id, owner, symbol, side, quantity, price, status, createdAt, executedAt);
    }

    /**
     * Returns a copy of the aggregate with an assigned id.
     *
     * @param id identifier generated after persistence
     * @return new instance with id
     */
    public TradeOrder withId(OrderId id) {
        return new TradeOrder(
                id,
                owner,
                symbol,
                side,
                quantity,
                price,
                status,
                createdAt,
                executedAt
        );
    }

    /**
     * Executes the order if it is pending.
     *
     * @return new order in {@link OrderStatus#EXECUTED} state
     * @throws TradingDomainException if the order is not pending
     */
    public TradeOrder execute() {
        if (status != OrderStatus.PENDING) {
            throw new TradingDomainException("Solo se pueden ejecutar ordenes pendientes");
        }
        return new TradeOrder(
                id,
                owner,
                symbol,
                side,
                quantity,
                price,
                OrderStatus.EXECUTED,
                createdAt,
                LocalDateTime.now()
        );
    }

    /**
     * Cancels the order if it is pending and requested by its owner.
     *
     * @param requestedBy user requesting cancellation
     * @return new order in {@link OrderStatus#CANCELLED} state
     * @throws TradingDomainException if the requester is not the owner or the order is not pending
     */
    public TradeOrder cancel(String requestedBy) {
        if (!owner.equals(requestedBy)) {
            throw new TradingDomainException("Solo el propietario puede cancelar la orden");
        }
        if (status != OrderStatus.PENDING) {
            throw new TradingDomainException("Solo se pueden cancelar ordenes pendientes");
        }
        return new TradeOrder(
                id,
                owner,
                symbol,
                side,
                quantity,
                price,
                OrderStatus.CANCELLED,
                createdAt,
                executedAt
        );
    }

    /**
     * Calculates the order total amount: price x quantity.
     *
     * @return order total amount
     */
    public Money totalAmount() {
        return price.multiply(quantity);
    }

    /** @return order id (can be null before persistence) */
    public OrderId id() { return id; }

    /** @return order owner */
    public String owner() { return owner; }

    /** @return normalized uppercase symbol */
    public String symbol() { return symbol; }

    /** @return order side */
    public OrderSide side() { return side; }

    /** @return order quantity */
    public Quantity quantity() { return quantity; }

    /** @return order unit price */
    public Money price() { return price; }

    /** @return current order status */
    public OrderStatus status() { return status; }

    /** @return creation date and time */
    public LocalDateTime createdAt() { return createdAt; }

    /** @return execution date and time, or null if it was not executed */
    public LocalDateTime executedAt() { return executedAt; }

    private static String validateOwner(String owner) {
        if (owner == null || owner.isBlank()) {
            throw new TradingDomainException("El propietario es obligatorio");
        }
        return owner;
    }

    private static String validateSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new TradingDomainException("El simbolo es obligatorio");
        }
        return symbol.toUpperCase();
    }
}
