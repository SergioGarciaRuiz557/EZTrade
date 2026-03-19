package com.trading.platform.eztrade.wallet.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad de dominio para gestionar efectivo disponible y efectivo reservado.
 */
public class WalletAccount {

    private final String owner;
    private final BigDecimal availableBalance;
    private final BigDecimal reservedBalance;

    private WalletAccount(String owner, BigDecimal availableBalance, BigDecimal reservedBalance) {
        this.owner = validateOwner(owner);
        this.availableBalance = requireNonNull(availableBalance, "Available balance is required");
        this.reservedBalance = requireNonNull(reservedBalance, "Reserved balance is required");
        validateNonNegative(this.availableBalance, "Available balance cannot be negative");
        validateNonNegative(this.reservedBalance, "Reserved balance cannot be negative");
    }

    public static WalletAccount open(String owner) {
        return new WalletAccount(owner, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public static WalletAccount rehydrate(String owner, BigDecimal availableBalance, BigDecimal reservedBalance) {
        return new WalletAccount(owner, availableBalance, reservedBalance);
    }

    public WalletAccount deposit(BigDecimal amount) {
        validateAmount(amount);
        return new WalletAccount(owner, availableBalance.add(amount), reservedBalance);
    }

    public WalletAccount withdraw(BigDecimal amount) {
        validateAmount(amount);
        ensureSufficientAvailable(amount);
        return new WalletAccount(owner, availableBalance.subtract(amount), reservedBalance);
    }

    public WalletAccount reserve(BigDecimal amount) {
        validateAmount(amount);
        ensureSufficientAvailable(amount);
        return new WalletAccount(owner, availableBalance.subtract(amount), reservedBalance.add(amount));
    }

    public WalletAccount release(BigDecimal amount) {
        validateAmount(amount);
        ensureSufficientReserved(amount);
        return new WalletAccount(owner, availableBalance.add(amount), reservedBalance.subtract(amount));
    }

    public WalletAccount settleReservedDebit(BigDecimal amount) {
        validateAmount(amount);
        ensureSufficientReserved(amount);
        return new WalletAccount(owner, availableBalance, reservedBalance.subtract(amount));
    }

    public WalletAccount settleCredit(BigDecimal amount) {
        validateAmount(amount);
        return new WalletAccount(owner, availableBalance.add(amount), reservedBalance);
    }

    public WalletAccount chargeFee(BigDecimal amount) {
        return withdraw(amount);
    }

    public String owner() {
        return owner;
    }

    public BigDecimal availableBalance() {
        return availableBalance;
    }

    public BigDecimal reservedBalance() {
        return reservedBalance;
    }

    private void ensureSufficientAvailable(BigDecimal amount) {
        if (availableBalance.compareTo(amount) < 0) {
            throw new WalletDomainException("Insufficient available funds");
        }
    }

    private void ensureSufficientReserved(BigDecimal amount) {
        if (reservedBalance.compareTo(amount) < 0) {
            throw new WalletDomainException("Insufficient reserved funds");
        }
    }

    private static String validateOwner(String owner) {
        if (owner == null || owner.isBlank()) {
            throw new WalletDomainException("Owner is required");
        }
        return owner;
    }

    private static void validateAmount(BigDecimal amount) {
        requireNonNull(amount, "Amount is required");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletDomainException("Amount must be greater than zero");
        }
    }

    private static void validateNonNegative(BigDecimal value, String message) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new WalletDomainException(message);
        }
    }

    private static <T> T requireNonNull(T value, String message) {
        return Objects.requireNonNull(value, message);
    }
}

