package com.trading.platform.eztrade.wallet.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad de dominio que representa la <strong>cuenta de efectivo</strong> (wallet) de un usuario.
 * <p>
 * Modela dos "bolsillos":
 * <ul>
 *   <li><strong>availableBalance</strong>: fondos disponibles para retirar o para reservar en una orden.</li>
 *   <li><strong>reservedBalance</strong>: fondos retenidos temporalmente (p. ej. al colocar una orden BUY) hasta que la orden
 *   se cancele (se liberan) o se ejecute (se liquidan).</li>
 * </ul>
 * <p>
 * La clase es <strong>inmutable</strong>: cada operación devuelve una nueva instancia con los balances actualizados. Esto
 * simplifica el razonamiento del dominio y reduce efectos colaterales; la persistencia se encarga en la capa de
 * aplicación/adaptadores.
 * <p>
 * <strong>Invariantes</strong>:
 * <ul>
 *   <li>{@code owner} no puede ser nulo ni vacío.</li>
 *   <li>Los balances nunca pueden ser negativos.</li>
 *   <li>Las operaciones que consumen fondos verifican suficiencia y lanzan {@link WalletDomainException} si no se cumple.</li>
 * </ul>
 */
public class WalletAccount {

    private final String owner;
    private final BigDecimal availableBalance;
    private final BigDecimal reservedBalance;

    /**
     * Constructor privado: fuerza el uso de factorías y asegura que todas las instancias cumplen invariantes.
     */
    private WalletAccount(String owner, BigDecimal availableBalance, BigDecimal reservedBalance) {
        this.owner = validateOwner(owner);
        this.availableBalance = requireNonNull(availableBalance, "Available balance is required");
        this.reservedBalance = requireNonNull(reservedBalance, "Reserved balance is required");
        validateNonNegative(this.availableBalance, "Available balance cannot be negative");
        validateNonNegative(this.reservedBalance, "Reserved balance cannot be negative");
    }

    /**
     * Abre una cuenta nueva para un owner (balances a cero).
     * <p>
     * Se usa cuando aún no existe registro persistido para el usuario.
     */
    public static WalletAccount open(String owner) {
        return new WalletAccount(owner, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * Reconstruye (rehydrate) la entidad desde persistencia.
     */
    public static WalletAccount rehydrate(String owner, BigDecimal availableBalance, BigDecimal reservedBalance) {
        return new WalletAccount(owner, availableBalance, reservedBalance);
    }

    /**
     * Ingresa fondos en el saldo disponible.
     */
    public WalletAccount deposit(BigDecimal amount) {
        validateAmount(amount);
        return new WalletAccount(owner, availableBalance.add(amount), reservedBalance);
    }

    /**
     * Retira fondos del saldo disponible.
     *
     * @throws WalletDomainException si no hay saldo disponible suficiente.
     */
    public WalletAccount withdraw(BigDecimal amount) {
        validateAmount(amount);
        ensureSufficientAvailable(amount);
        return new WalletAccount(owner, availableBalance.subtract(amount), reservedBalance);
    }

    /**
     * Reserva fondos: reduce disponible y aumenta reservado.
     * <p>
     * Se utiliza para órdenes BUY antes de ejecutarse.
     */
    public WalletAccount reserve(BigDecimal amount) {
        validateAmount(amount);
        ensureSufficientAvailable(amount);
        return new WalletAccount(owner, availableBalance.subtract(amount), reservedBalance.add(amount));
    }

    /**
     * Libera fondos reservados: aumenta disponible y reduce reservado.
     * <p>
     * Se utiliza al cancelar una orden o cuando la ejecución requiere menos efectivo del que se reservó inicialmente.
     */
    public WalletAccount release(BigDecimal amount) {
        validateAmount(amount);
        ensureSufficientReserved(amount);
        return new WalletAccount(owner, availableBalance.add(amount), reservedBalance.subtract(amount));
    }

    /**
     * Liquida una compra (BUY) consumiendo saldo reservado.
     * <p>
     * En este caso el disponible no cambia aquí, porque ya se descontó al reservar.
     */
    public WalletAccount settleReservedDebit(BigDecimal amount) {
        validateAmount(amount);
        ensureSufficientReserved(amount);
        return new WalletAccount(owner, availableBalance, reservedBalance.subtract(amount));
    }

    /**
     * Liquida una venta (SELL) abonando en saldo disponible.
     */
    public WalletAccount settleCredit(BigDecimal amount) {
        validateAmount(amount);
        return new WalletAccount(owner, availableBalance.add(amount), reservedBalance);
    }

    /**
     * Aplica una comisión.
     * <p>
     * Se modela como un retiro del disponible para reutilizar validaciones de importe y suficiencia.
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

