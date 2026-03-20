package com.trading.platform.eztrade.wallet.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro auditable e inmutable de un movimiento monetario del wallet.
 * <p>
 * Este modelo sustituye el término "Ledger" por "Transaction" para hacerlo más intuitivo: en la práctica sigue
 * representando una entrada del histórico de movimientos (libro mayor/ledger) y contiene deltas y balances resultantes.
 * <p>
 * La combinación (owner, referenceId, movementType) se usa como clave natural para idempotencia.
 */
public record WalletTransaction(
        Long id,
        /** Identificador del usuario/propietario del wallet. */
        String owner,
        /** Clasificación del movimiento (depósito, reserva, liberación, liquidación, etc.). */
        MovementType movementType,
        /** Importe principal del movimiento (siempre positivo en el dominio). */
        BigDecimal amount,
        /** Variación aplicada al saldo disponible (puede ser positiva, cero o negativa). */
        BigDecimal availableDelta,
        /** Variación aplicada al saldo reservado (puede ser positiva, cero o negativa). */
        BigDecimal reservedDelta,
        /** Saldo disponible resultante tras aplicar el movimiento. */
        BigDecimal availableBalanceAfter,
        /** Saldo reservado resultante tras aplicar el movimiento. */
        BigDecimal reservedBalanceAfter,
        /** Tipo/origen de la referencia (orden, ajuste manual...). */
        ReferenceType referenceType,
        /** Identificador de referencia (p. ej. orderId o identificador externo del ajuste manual). */
        String referenceId,
        /** Texto libre para facilitar auditoría/histórico. */
        String description,
        /** Momento "efectivo" del movimiento (normalmente el de ocurrencia del evento que lo dispara). */
        LocalDateTime occurredAt
) {

    /**
     * Constructor canónico del record.
     * <p>
     * Aquí se validan invariantes para asegurar que cualquier instancia es consistente.
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
     * Factoría para crear una transacción nueva (sin id), dejando que la persistencia asigne el identificador.
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

