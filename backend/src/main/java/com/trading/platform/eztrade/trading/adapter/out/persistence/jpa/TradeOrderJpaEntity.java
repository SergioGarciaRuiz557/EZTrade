package com.trading.platform.eztrade.trading.adapter.out.persistence.jpa;

import com.trading.platform.eztrade.trading.domain.OrderSide;
import com.trading.platform.eztrade.trading.domain.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Infrastructure JPA entity used to persist trading orders.
 * <p>
 * This class does not contain business rules; domain behavior lives in
 * {@code TradeOrder}.
 */
@Entity
@Table(name = "trade_order")
public class TradeOrderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide side;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime executedAt;

    /** @return persisted id */
    public Long getId() { return id; }

    /** @param id persisted id */
    public void setId(Long id) { this.id = id; }

    /** @return order owner */
    public String getOwner() { return owner; }

    /** @param owner order owner */
    public void setOwner(String owner) { this.owner = owner; }

    /** @return asset symbol */
    public String getSymbol() { return symbol; }

    /** @param symbol asset symbol */
    public void setSymbol(String symbol) { this.symbol = symbol; }

    /** @return order side */
    public OrderSide getSide() { return side; }

    /** @param side order side */
    public void setSide(OrderSide side) { this.side = side; }

    /** @return requested quantity */
    public BigDecimal getQuantity() { return quantity; }

    /** @param quantity requested quantity */
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    /** @return unit price */
    public BigDecimal getPrice() { return price; }

    /** @param price unit price */
    public void setPrice(BigDecimal price) { this.price = price; }

    /** @return order status */
    public OrderStatus getStatus() { return status; }

    /** @param status order status */
    public void setStatus(OrderStatus status) { this.status = status; }

    /** @return creation timestamp */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** @param createdAt creation timestamp */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** @return execution timestamp, when applicable */
    public LocalDateTime getExecutedAt() { return executedAt; }

    /** @param executedAt execution timestamp, when applicable */
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
}
