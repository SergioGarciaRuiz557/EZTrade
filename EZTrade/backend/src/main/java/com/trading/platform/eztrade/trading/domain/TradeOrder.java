package com.trading.platform.eztrade.trading.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Agregado raiz del modulo trading.
 * <p>
 * Modela una orden de compra/venta y concentra sus invariantes:
 * propietario valido, simbolo valido, cantidades/precios positivos y
 * transiciones de estado permitidas.
 * <p>
 * Esta clase pertenece al dominio puro y no depende de Spring ni JPA.
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
        this.side = Objects.requireNonNull(side, "Order side is required");
        this.quantity = Objects.requireNonNull(quantity, "Quantity is required");
        this.price = Objects.requireNonNull(price, "Price is required");
        this.status = Objects.requireNonNull(status, "Order status is required");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt is required");
        this.executedAt = executedAt;
    }

    /**
     * Fabrica para crear una nueva orden en estado {@link OrderStatus#PENDING}.
     *
     * @param owner email/identificador del propietario de la orden
     * @param symbol simbolo del activo
     * @param side tipo de orden (compra o venta)
     * @param quantity cantidad solicitada
     * @param price precio unitario
     * @return nueva orden pendiente
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
     * Fabrica para reconstruir el agregado desde persistencia.
     *
     * @param id id de la orden
     * @param owner propietario
     * @param symbol simbolo
     * @param side tipo de orden
     * @param quantity cantidad
     * @param price precio unitario
     * @param status estado actual
     * @param createdAt fecha de creacion
     * @param executedAt fecha de ejecucion (puede ser null)
     * @return agregado rehidratado
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
     * Devuelve una copia del agregado con id asignado.
     *
     * @param id identificador generado tras persistencia
     * @return nueva instancia con id
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
     * Ejecuta la orden si esta pendiente.
     *
     * @return nueva orden en estado {@link OrderStatus#EXECUTED}
     * @throws TradingDomainException si la orden no esta pendiente
     */
    public TradeOrder execute() {
        if (status != OrderStatus.PENDING) {
            throw new TradingDomainException("Only pending orders can be executed");
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
     * Cancela la orden si esta pendiente y la solicita su propietario.
     *
     * @param requestedBy usuario que solicita la cancelacion
     * @return nueva orden en estado {@link OrderStatus#CANCELLED}
     * @throws TradingDomainException si no es el propietario o no esta pendiente
     */
    public TradeOrder cancel(String requestedBy) {
        if (!owner.equals(requestedBy)) {
            throw new TradingDomainException("Only the owner can cancel the order");
        }
        if (status != OrderStatus.PENDING) {
            throw new TradingDomainException("Only pending orders can be cancelled");
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
     * Calcula el importe total de la orden: precio x cantidad.
     *
     * @return monto total de la orden
     */
    public Money totalAmount() {
        return price.multiply(quantity);
    }

    /** @return id de la orden (puede ser null antes de persistir) */
    public OrderId id() { return id; }

    /** @return propietario de la orden */
    public String owner() { return owner; }

    /** @return simbolo normalizado en mayusculas */
    public String symbol() { return symbol; }

    /** @return tipo de orden */
    public OrderSide side() { return side; }

    /** @return cantidad de la orden */
    public Quantity quantity() { return quantity; }

    /** @return precio unitario de la orden */
    public Money price() { return price; }

    /** @return estado actual de la orden */
    public OrderStatus status() { return status; }

    /** @return fecha y hora de creacion */
    public LocalDateTime createdAt() { return createdAt; }

    /** @return fecha y hora de ejecucion, o null si no fue ejecutada */
    public LocalDateTime executedAt() { return executedAt; }

    private static String validateOwner(String owner) {
        if (owner == null || owner.isBlank()) {
            throw new TradingDomainException("Owner is required");
        }
        return owner;
    }

    private static String validateSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new TradingDomainException("Symbol is required");
        }
        return symbol.toUpperCase();
    }
}
