package com.trading.platform.eztrade.wallet.adapter.out.persistence.jpa;

import com.trading.platform.eztrade.wallet.domain.MovementType;
import com.trading.platform.eztrade.wallet.domain.ReferenceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "wallet_ledger_entry",
        indexes = {
                @Index(name = "idx_wallet_ledger_owner_occurred", columnList = "owner, occurredAt")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_wallet_ledger_owner_ref_movement", columnNames = {"owner", "referenceId", "movementType"})
        }
)
public class LedgerEntryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType movementType;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal availableDelta;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal reservedDelta;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal availableBalanceAfter;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal reservedBalanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferenceType referenceType;

    @Column(nullable = false)
    private String referenceId;

    @Column
    private String description;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

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

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAvailableDelta() {
        return availableDelta;
    }

    public void setAvailableDelta(BigDecimal availableDelta) {
        this.availableDelta = availableDelta;
    }

    public BigDecimal getReservedDelta() {
        return reservedDelta;
    }

    public void setReservedDelta(BigDecimal reservedDelta) {
        this.reservedDelta = reservedDelta;
    }

    public BigDecimal getAvailableBalanceAfter() {
        return availableBalanceAfter;
    }

    public void setAvailableBalanceAfter(BigDecimal availableBalanceAfter) {
        this.availableBalanceAfter = availableBalanceAfter;
    }

    public BigDecimal getReservedBalanceAfter() {
        return reservedBalanceAfter;
    }

    public void setReservedBalanceAfter(BigDecimal reservedBalanceAfter) {
        this.reservedBalanceAfter = reservedBalanceAfter;
    }

    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(ReferenceType referenceType) {
        this.referenceType = referenceType;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}

