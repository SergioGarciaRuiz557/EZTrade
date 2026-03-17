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
 * Entidad JPA de infraestructura para persistir ordenes de trading.
 * <p>
 * Esta clase no contiene reglas de negocio; el comportamiento de dominio
 * vive en {@code TradeOrder}.
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

    /** @return id persistido */
    public Long getId() { return id; }

    /** @param id id persistido */
    public void setId(Long id) { this.id = id; }

    /** @return propietario de la orden */
    public String getOwner() { return owner; }

    /** @param owner propietario de la orden */
    public void setOwner(String owner) { this.owner = owner; }

    /** @return simbolo del activo */
    public String getSymbol() { return symbol; }

    /** @param symbol simbolo del activo */
    public void setSymbol(String symbol) { this.symbol = symbol; }

    /** @return lado de la orden */
    public OrderSide getSide() { return side; }

    /** @param side lado de la orden */
    public void setSide(OrderSide side) { this.side = side; }

    /** @return cantidad solicitada */
    public BigDecimal getQuantity() { return quantity; }

    /** @param quantity cantidad solicitada */
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    /** @return precio unitario */
    public BigDecimal getPrice() { return price; }

    /** @param price precio unitario */
    public void setPrice(BigDecimal price) { this.price = price; }

    /** @return estado de la orden */
    public OrderStatus getStatus() { return status; }

    /** @param status estado de la orden */
    public void setStatus(OrderStatus status) { this.status = status; }

    /** @return fecha de creacion */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** @param createdAt fecha de creacion */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** @return fecha de ejecucion, si aplica */
    public LocalDateTime getExecutedAt() { return executedAt; }

    /** @param executedAt fecha de ejecucion, si aplica */
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
}
