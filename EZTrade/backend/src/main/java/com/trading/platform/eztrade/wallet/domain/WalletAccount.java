package com.trading.platform.eztrade.wallet.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Domain entity that represents a user's <strong>cash account</strong> (wallet).
 * <p>
 * Models two "pockets":
 * <ul>
 *   <li><strong>availableBalance</strong>: funds available to withdraw or reserve for an order.</li>
 *   <li><strong>reservedBalance</strong>: funds temporarily held (for example, when placing a BUY order) until the order
 *   is cancelled (released) or executed (settled).</li>
 * </ul>
 * <p>
 * The class is <strong>immutable</strong>: each operation returns a new instance
 * with updated balances. This simplifies domain reasoning and reduces side
 * effects; persistence is handled by the application/adapters layer.
 * <p>
 * <strong>Invariants</strong>:
 * <ul>
 *   <li>{@code owner} cannot be null or blank.</li>
 *   <li>Balances can never be negative.</li>
 *   <li>Operations that consume funds check sufficiency and throw {@link WalletDomainException} if the invariant is not met.</li>
 * </ul>
 */
public class WalletAccount {

    private final String owner;
    private final BigDecimal availableBalance;
    private final BigDecimal reservedBalance;

    /**
     * Private constructor: forces factory usage and ensures every instance satisfies invariants.
     */
    private WalletAccount(String owner, BigDecimal availableBalance, BigDecimal reservedBalance) {
        this.owner = validateOwner(owner);
        this.availableBalance = requireNonNull(availableBalance, "Available balance is required");
        this.reservedBalance = requireNonNull(reservedBalance, "Reserved balance is required");
        validateNonNegative(this.availableBalance, "Available balance cannot be negative");
        validateNonNegative(this.reservedBalance, "Reserved balance cannot be negative");
    }

    /**
     * Opens a new account for an owner (zero balances).
     * <p>
     * Used when no persisted record exists for the user yet.
     */
    public static WalletAccount open(String owner) {
        return new WalletAccount(owner, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * Rehydrates the entity from persistence.
     */
    public static WalletAccount rehydrate(String owner, BigDecimal availableBalance, BigDecimal reservedBalance) {
        return new WalletAccount(owner, availableBalance, reservedBalance);
    }

    /**
     * Deposits funds into the available balance.
     */
    public WalletAccount deposit(BigDecimal amount) {
        validateAmount(amount);
        return new WalletAccount(owner, availableBalance.add(amount), reservedBalance);
    }

    /**
     * Withdraws funds from the available balance.
     *
     * @throws WalletDomainException if there is not enough available balance
     */
    public WalletAccount withdraw(BigDecimal amount) {
        validateAmount(amount);
        ensureSufficientAvailable(amount);
        return new WalletAccount(owner, availableBalance.subtract(amount), reservedBalance);
    }

    /**
     * Reserves funds: decreases available balance and increases reserved balance.
     * <p>
     * Used for BUY orders before they are executed.
     */
    public WalletAccount reserve(BigDecimal amount) {
        validateAmount(amount);
        ensureSufficientAvailable(amount);
        return new WalletAccount(owner, availableBalance.subtract(amount), reservedBalance.add(amount));
    }

    /**
     * Releases reserved funds: increases available balance and decreases reserved balance.
     * <p>
     * Used when cancelling an order or when execution requires less cash than was initially reserved.
     */
    public WalletAccount release(BigDecimal amount) {
        validateAmount(amount);
        ensureSufficientReserved(amount);
        return new WalletAccount(owner, availableBalance.add(amount), reservedBalance.subtract(amount));
    }

    /**
     * Settles a purchase (BUY) by consuming reserved balance.
     * <p>
     * In this case the available balance does not change here, because it was
     * already deducted when reserving.
     */
    public WalletAccount settleReservedDebit(BigDecimal amount) {
        validateAmount(amount);
        ensureSufficientReserved(amount);
        return new WalletAccount(owner, availableBalance, reservedBalance.subtract(amount));
    }

    /**
     * Settles a sale (SELL) by crediting the available balance.
     */
    public WalletAccount settleCredit(BigDecimal amount) {
        validateAmount(amount);
        return new WalletAccount(owner, availableBalance.add(amount), reservedBalance);
    }

    /**
     * Applies a fee.
     * <p>
     * Modeled as a withdrawal from the available balance to reuse amount and
     * sufficiency validations.
     */
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

