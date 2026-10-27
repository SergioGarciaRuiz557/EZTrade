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
 * JPA entity for persisting the current state of a wallet account.
 * <p>
 * Note: the domain model is immutable
 * ({@link com.trading.platform.eztrade.wallet.domain.WalletAccount}), while
 * this entity is mutable because JPA requires it.
 */
public class WalletAccountJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    /** Owner is unique: there is a single wallet account per user. */
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

