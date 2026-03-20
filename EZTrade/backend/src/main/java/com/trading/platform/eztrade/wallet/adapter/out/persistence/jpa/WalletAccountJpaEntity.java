package com.trading.platform.eztrade.wallet.adapter.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "wallet_account")
/**
 * Entidad JPA para persistir el estado actual de una cuenta wallet.
 * <p>
 * Nota: el modelo de dominio es inmutable ({@link com.trading.platform.eztrade.wallet.domain.WalletAccount}), mientras
 * que esta entidad es mutable por requisitos de JPA.
 */
public class WalletAccountJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    /** Owner es único: hay una única cuenta wallet por usuario. */
    private String owner;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal availableBalance;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal reservedBalance;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public BigDecimal getReservedBalance() {
        return reservedBalance;
    }

    public void setReservedBalance(BigDecimal reservedBalance) {
        this.reservedBalance = reservedBalance;
    }
}

