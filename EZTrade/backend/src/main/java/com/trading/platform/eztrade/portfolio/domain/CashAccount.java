package com.trading.platform.eztrade.portfolio.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad de dominio para el cash disponible de un usuario.
 */
public class CashAccount {

    private final String owner;
    private final BigDecimal availableCash;

    private CashAccount(String owner, BigDecimal availableCash) {
        this.owner = validateOwner(owner);
        this.availableCash = requireNotNull(availableCash, "Available cash is required");
    }

    public static CashAccount open(String owner) {
        return new CashAccount(owner, BigDecimal.ZERO);
    }

    public static CashAccount rehydrate(String owner, BigDecimal availableCash) {
        return new CashAccount(owner, availableCash);
    }

    public CashAccount credit(BigDecimal amount) {
        validateAmount(amount);
        return new CashAccount(owner, availableCash.add(amount));
    }

    public CashAccount debit(BigDecimal amount) {
        validateAmount(amount);
        return new CashAccount(owner, availableCash.subtract(amount));
    }

    public String owner() {
        return owner;
    }

    public BigDecimal availableCash() {
        return availableCash;
    }

    private static String validateOwner(String owner) {
        if (owner == null || owner.isBlank()) {
            throw new PortfolioDomainException("Owner is required");
        }
        return owner;
    }

    private static void validateAmount(BigDecimal amount) {
        requireNotNull(amount, "Amount is required");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PortfolioDomainException("Amount must be greater than zero");
        }
    }

    private static <T> T requireNotNull(T value, String message) {
        return Objects.requireNonNull(value, message);
    }
}

