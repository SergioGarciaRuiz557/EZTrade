package com.trading.platform.eztrade.wallet.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro auditable e inmutable de un movimiento del wallet.
 */
public record LedgerEntry(
        Long id,
        String owner,
        MovementType movementType,
        BigDecimal amount,
        BigDecimal availableDelta,
        BigDecimal reservedDelta,
        BigDecimal availableBalanceAfter,
        BigDecimal reservedBalanceAfter,
        ReferenceType referenceType,
        String referenceId,
        String description,
        LocalDateTime occurredAt
) {

    public LedgerEntry {
        owner = validateOwner(owner);
        movementType = Objects.requireNonNull(movementType, "Movement type is required");
        amount = validatePositive(amount, "Amount must be greater than zero");
        availableDelta = Objects.requireNonNull(availableDelta, "Available delta is required");
        reservedDelta = Objects.requireNonNull(reservedDelta, "Reserved delta is required");
        availableBalanceAfter = validateNonNegative(availableBalanceAfter, "Available balance after cannot be negative");
        reservedBalanceAfter = validateNonNegative(reservedBalanceAfter, "Reserved balance after cannot be negative");
        referenceType = Objects.requireNonNull(referenceType, "Reference type is required");
        if (referenceId == null || referenceId.isBlank()) {
            throw new WalletDomainException("Reference id is required");
        }
        occurredAt = Objects.requireNonNull(occurredAt, "OccurredAt is required");
    }

    public static LedgerEntry newEntry(String owner,
                                       MovementType movementType,
                                       BigDecimal amount,
                                       BigDecimal availableDelta,
                                       BigDecimal reservedDelta,
                                       BigDecimal availableBalanceAfter,
                                       BigDecimal reservedBalanceAfter,
                                       ReferenceType referenceType,
                                       String referenceId,
                                       String description,
                                       LocalDateTime occurredAt) {
        return new LedgerEntry(
                null,
                owner,
                movementType,
                amount,
                availableDelta,
                reservedDelta,
                availableBalanceAfter,
                reservedBalanceAfter,
                referenceType,
                referenceId,
                description,
                occurredAt
        );
    }

    private static String validateOwner(String owner) {
        if (owner == null || owner.isBlank()) {
            throw new WalletDomainException("Owner is required");
        }
        return owner;
    }

    private static BigDecimal validatePositive(BigDecimal value, String message) {
        Objects.requireNonNull(value, message);
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletDomainException(message);
        }
        return value;
    }

    private static BigDecimal validateNonNegative(BigDecimal value, String message) {
        Objects.requireNonNull(value, message);
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new WalletDomainException(message);
        }
        return value;
    }
}

