package com.trading.platform.eztrade.wallet.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Auditable and immutable record of a wallet monetary movement.
 * <p>
 * This model replaces the term "Ledger" with "Transaction" to make it more
 * intuitive: in practice it still represents a movement-history entry
 * (ledger) and contains deltas and resulting balances.
 * <p>
 * The combination (owner, referenceId, movementType) is used as a natural key
 * for idempotency.
 */
public record WalletTransaction(
        Long id,
        /** User/wallet owner identifier. */
        String owner,
        /** Movement classification (deposit, reserve, release, settlement, etc.). */
        MovementType movementType,
        /** Main movement amount (always positive in the domain). */
        BigDecimal amount,
        /** Variation applied to the available balance (can be positive, zero, or negative). */
        BigDecimal availableDelta,
        /** Variation applied to the reserved balance (can be positive, zero, or negative). */
        BigDecimal reservedDelta,
        /** Available balance after applying the movement. */
        BigDecimal availableBalanceAfter,
        /** Reserved balance after applying the movement. */
        BigDecimal reservedBalanceAfter,
        /** Reference type/source (order, manual adjustment, etc.). */
        ReferenceType referenceType,
        /** Reference identifier (for example, orderId or external manual-adjustment identifier). */
        String referenceId,
        /** Free text to support auditing/history. */
        String description,
        /** "Effective" movement time, usually when the triggering event occurred. */
        LocalDateTime occurredAt
) {

    /**
     * Canonical record constructor.
     * <p>
     * Validates invariants here to ensure every instance is consistent.
     */
    public WalletTransaction {
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

    /**
     * Factory for creating a new transaction (without id), letting persistence assign the identifier.
     */
    public static WalletTransaction newEntry(String owner,
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
        return new WalletTransaction(
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

