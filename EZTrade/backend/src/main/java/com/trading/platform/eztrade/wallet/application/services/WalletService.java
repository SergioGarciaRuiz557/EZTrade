package com.trading.platform.eztrade.wallet.application.services;

import com.trading.platform.eztrade.trading.domain.events.OrderCancelledEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderPlacedEvent;
import com.trading.platform.eztrade.wallet.application.ports.in.AdjustWalletFundsUseCase;
import com.trading.platform.eztrade.wallet.application.ports.in.GetWalletBalanceUseCase;
import com.trading.platform.eztrade.wallet.application.ports.in.HandleOrderCancelledUseCase;
import com.trading.platform.eztrade.wallet.application.ports.in.HandleOrderExecutedUseCase;
import com.trading.platform.eztrade.wallet.application.ports.in.HandleOrderPlacedUseCase;
import com.trading.platform.eztrade.wallet.application.ports.out.DomainEventPublisherPort;
import com.trading.platform.eztrade.wallet.application.ports.out.WalletTransactionRepositoryPort;
import com.trading.platform.eztrade.wallet.application.ports.out.WalletAccountRepositoryPort;
import com.trading.platform.eztrade.wallet.domain.WalletTransaction;
import com.trading.platform.eztrade.wallet.domain.MovementType;
import com.trading.platform.eztrade.wallet.domain.ReferenceType;
import com.trading.platform.eztrade.wallet.domain.WalletAccount;
import com.trading.platform.eztrade.wallet.domain.WalletDomainException;
import com.trading.platform.eztrade.wallet.domain.events.AvailableCashUpdatedEvent;
import com.trading.platform.eztrade.wallet.domain.events.FundsReleasedEvent;
import com.trading.platform.eztrade.wallet.domain.events.FundsReservedEvent;
import com.trading.platform.eztrade.wallet.domain.events.FundsSettledEvent;
import com.trading.platform.eztrade.wallet.domain.events.InsufficientFundsEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Servicio de aplicación que implementa los casos de uso del módulo Wallet.
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li><strong>Reaccionar a eventos de trading</strong>:
 *     <ul>
 *       <li>{@link #handle(OrderPlacedEvent)}: reservar fondos para BUY.</li>
 *       <li>{@link #handle(OrderCancelledEvent)}: liberar fondos reservados.</li>
 *       <li>{@link #handle(OrderExecutedEvent)}: liquidar BUY/SELL.</li>
 *     </ul>
 *   </li>
 *   <li><strong>Ajustes manuales</strong> (depósito/retiro/comisión) vía {@link AdjustWalletFundsUseCase}.</li>
 *   <li><strong>Auditoría</strong>: por cada cambio persiste una {@link WalletTransaction} con deltas y balances finales.</li>
 *   <li><strong>Publicación de eventos de dominio</strong> tras operaciones relevantes (reservado/liberado/liquidado o fondos insuficientes).</li>
 * </ul>
 * <p>
 * <strong>Consistencia e idempotencia</strong>:
 * <ul>
 *   <li>Se ejecuta dentro de una transacción ({@link Transactional}) para que actualización de cuenta + ledger sea atómica.</li>
 *   <li>Intenta ser idempotente comprobando si ya existe una entrada de ledger con (owner, referenceId, movementType).
 *   Esto protege ante eventos duplicados o reintentos.</li>
 *   <li>La carga de cuenta usa un método "for update" para mitigar carreras cuando hay concurrencia sobre el mismo owner.</li>
 * </ul>
 */
@Service
@Transactional
public class WalletService implements HandleOrderPlacedUseCase,
        HandleOrderCancelledUseCase,
        HandleOrderExecutedUseCase,
        AdjustWalletFundsUseCase,
        GetWalletBalanceUseCase {

    private static final String ORDER_SETTLEMENT_DEBIT = "DEBIT";
    private static final String ORDER_SETTLEMENT_CREDIT = "CREDIT";

    private final WalletAccountRepositoryPort walletAccountRepository;
    private final WalletTransactionRepositoryPort ledgerEntryRepository;
    private final DomainEventPublisherPort eventPublisher;

    public WalletService(WalletAccountRepositoryPort walletAccountRepository,
                         WalletTransactionRepositoryPort ledgerEntryRepository,
                         DomainEventPublisherPort eventPublisher) {
        this.walletAccountRepository = walletAccountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void handle(OrderPlacedEvent event) {
        // Solo las BUY requieren reservar efectivo. Las SELL no consumen efectivo en este módulo.
        if (!"BUY".equalsIgnoreCase(event.side())) {
            return;
        }

        String owner = validateOwner(event.owner());
        String orderRef = String.valueOf(event.orderId());
        BigDecimal amount = orderAmount(event.quantity(), event.price());

        // Idempotencia: si ya existe una reserva para esa orden, no repetir.
        if (ledgerEntryRepository.existsByOwnerAndReferenceIdAndMovementType(owner, orderRef, MovementType.RESERVE)) {
            return;
        }

        // Cargamos con bloqueo para evitar modificaciones concurrentes del mismo wallet.
        WalletAccount account = lockOrOpenAccount(owner);
        if (account.availableBalance().compareTo(amount) < 0) {
            publishInsufficientFunds(orderRef, owner, amount, account, "Available balance is not enough to reserve funds");
            return;
        }

        // Aplicamos el cambio de dominio y persistimos.
        WalletAccount updated = walletAccountRepository.save(account.reserve(amount));

        // Registramos en el ledger con deltas explícitos para auditoría.
        ledgerEntryRepository.save(WalletTransaction.newEntry(
                owner,
                MovementType.RESERVE,
                amount,
                amount.negate(),
                amount,
                updated.availableBalance(),
                updated.reservedBalance(),
                ReferenceType.ORDER,
                orderRef,
                "Funds reserved for order",
                now(event.occurredAt())
        ));

        // Publicamos evento de dominio para que otros módulos reaccionen.
        eventPublisher.publish(new FundsReservedEvent(
                orderRef,
                owner,
                amount,
                updated.availableBalance(),
                updated.reservedBalance(),
                LocalDateTime.now()
        ));
        publishAvailableCashUpdated(owner, updated.availableBalance(), "ORDER_PLACED", orderRef, now(event.occurredAt()));
    }

    @Override
    public void handle(OrderCancelledEvent event) {
        String owner = validateOwner(event.owner());
        String orderRef = String.valueOf(event.orderId());

        // Idempotencia: si ya se liberó anteriormente, salir.
        if (ledgerEntryRepository.existsByOwnerAndReferenceIdAndMovementType(owner, orderRef, MovementType.RELEASE)) {
            return;
        }

        WalletTransaction reserveEntry = ledgerEntryRepository
                .findByOwnerAndReferenceIdAndMovementType(owner, orderRef, MovementType.RESERVE)
                .orElse(null);

        // Si no hay reserva registrada, no hay nada que liberar.
        if (reserveEntry == null) {
            return;
        }

        // Si ya se liquidó (BUY) no debemos liberar, porque los fondos ya se consumieron.
        if (ledgerEntryRepository.existsByOwnerAndReferenceIdAndMovementType(owner, orderRef, MovementType.SETTLEMENT_DEBIT)) {
            return;
        }

        WalletAccount account = lockOrOpenAccount(owner);
        BigDecimal amountToRelease = reserveEntry.amount();

        WalletAccount updated = walletAccountRepository.save(account.release(amountToRelease));

        ledgerEntryRepository.save(WalletTransaction.newEntry(
                owner,
                MovementType.RELEASE,
                amountToRelease,
                amountToRelease,
                amountToRelease.negate(),
                updated.availableBalance(),
                updated.reservedBalance(),
                ReferenceType.ORDER,
                orderRef,
                "Funds released after order cancellation",
                now(event.occurredAt())
        ));

        eventPublisher.publish(new FundsReleasedEvent(
                orderRef,
                owner,
                amountToRelease,
                updated.availableBalance(),
                updated.reservedBalance(),
                LocalDateTime.now()
        ));
    }

    @Override
    public void handle(OrderExecutedEvent event) {
        // Normalizamos el side a enum y delegamos en el flujo correspondiente.
        Side side = parseSide(event.side());
        switch (side) {
            case BUY -> settleBuy(event);
            case SELL -> settleSell(event);
        }

        String owner = validateOwner(event.owner());
        String orderRef = String.valueOf(event.orderId());
        BigDecimal availableCash = getBalance(owner).availableBalance();
        publishAvailableCashUpdated(owner, availableCash, "ORDER_EXECUTED", orderRef, now(event.occurredAt()));
    }

    @Override
    public void deposit(AdjustCommand command) {
        // Ajuste manual: se registra en ledger y es idempotente por referenceId + MovementType.
        processManualCommand(command, MovementType.DEPOSIT, "Manual deposit", account -> account.deposit(command.amount()));
    }

    @Override
    public void withdraw(AdjustCommand command) {
        processManualCommand(command, MovementType.WITHDRAWAL, "Manual withdrawal", account -> account.withdraw(command.amount()));
    }

    @Override
    public void chargeFee(AdjustCommand command) {
        processManualCommand(command, MovementType.FEE, "Fee charged", account -> account.chargeFee(command.amount()));
    }

    @Override
    public BalanceView getBalance(String owner) {
        String validatedOwner = validateOwner(owner);
        WalletAccount account = walletAccountRepository.findByOwner(validatedOwner)
                .orElseGet(() -> WalletAccount.open(validatedOwner));
        return new BalanceView(account.availableBalance(), account.reservedBalance());
    }

    private void settleBuy(OrderExecutedEvent event) {
        String owner = validateOwner(event.owner());
        String orderRef = String.valueOf(event.orderId());
        BigDecimal amount = orderAmount(event.quantity(), event.price());

        // Idempotencia: si esta liquidación ya se aplicó, no repetir.
        if (ledgerEntryRepository.existsByOwnerAndReferenceIdAndMovementType(owner, orderRef, MovementType.SETTLEMENT_DEBIT)) {
            return;
        }

        WalletTransaction reserveEntry = ledgerEntryRepository
                .findByOwnerAndReferenceIdAndMovementType(owner, orderRef, MovementType.RESERVE)
                .orElse(null);

        // Validación adicional: debe existir una reserva previa y cubrir el importe ejecutado.
        if (reserveEntry == null || reserveEntry.amount().compareTo(amount) < 0) {
            WalletAccount account = lockOrOpenAccount(owner);
            publishInsufficientFunds(orderRef, owner, amount, account, "Reserved funds are not enough to settle buy order");
            throw new WalletDomainException("Insufficient wallet funds to execute buy order " + orderRef);
        }

        WalletAccount workingAccount = lockOrOpenAccount(owner);

        // En ocasiones la ejecución real puede ser inferior a lo estimado al reservar (p. ej. precio final menor).
        // Esa diferencia se libera de nuevo al disponible.
        BigDecimal releaseDelta = reserveEntry.amount().subtract(amount);
        if (releaseDelta.compareTo(BigDecimal.ZERO) > 0
                && !ledgerEntryRepository.existsByOwnerAndReferenceIdAndMovementType(owner, orderRef, MovementType.RELEASE)) {
            workingAccount = walletAccountRepository.save(workingAccount.release(releaseDelta));
            ledgerEntryRepository.save(WalletTransaction.newEntry(
                    owner,
                    MovementType.RELEASE,
                    releaseDelta,
                    releaseDelta,
                    releaseDelta.negate(),
                    workingAccount.availableBalance(),
                    workingAccount.reservedBalance(),
                    ReferenceType.ORDER,
                    orderRef,
                    "Funds released due to execution at lower amount",
                    now(event.occurredAt())
            ));
            eventPublisher.publish(new FundsReleasedEvent(
                    orderRef,
                    owner,
                    releaseDelta,
                    workingAccount.availableBalance(),
                    workingAccount.reservedBalance(),
                    LocalDateTime.now()
            ));
        }

        WalletAccount settled = walletAccountRepository.save(workingAccount.settleReservedDebit(amount));

        ledgerEntryRepository.save(WalletTransaction.newEntry(
                owner,
                MovementType.SETTLEMENT_DEBIT,
                amount,
                BigDecimal.ZERO,
                amount.negate(),
                settled.availableBalance(),
                settled.reservedBalance(),
                ReferenceType.ORDER,
                orderRef,
                "Buy order settled against reserved funds",
                now(event.occurredAt())
        ));

        eventPublisher.publish(new FundsSettledEvent(
                orderRef,
                owner,
                amount,
                ORDER_SETTLEMENT_DEBIT,
                settled.availableBalance(),
                settled.reservedBalance(),
                LocalDateTime.now()
        ));
    }

    private void settleSell(OrderExecutedEvent event) {
        String owner = validateOwner(event.owner());
        String orderRef = String.valueOf(event.orderId());
        BigDecimal amount = orderAmount(event.quantity(), event.price());

        // Idempotencia: no abonar dos veces una misma venta.
        if (ledgerEntryRepository.existsByOwnerAndReferenceIdAndMovementType(owner, orderRef, MovementType.SETTLEMENT_CREDIT)) {
            return;
        }

        WalletAccount account = lockOrOpenAccount(owner);
        WalletAccount settled = walletAccountRepository.save(account.settleCredit(amount));

        ledgerEntryRepository.save(WalletTransaction.newEntry(
                owner,
                MovementType.SETTLEMENT_CREDIT,
                amount,
                amount,
                BigDecimal.ZERO,
                settled.availableBalance(),
                settled.reservedBalance(),
                ReferenceType.ORDER,
                orderRef,
                "Sell order settled in available funds",
                now(event.occurredAt())
        ));

        eventPublisher.publish(new FundsSettledEvent(
                orderRef,
                owner,
                amount,
                ORDER_SETTLEMENT_CREDIT,
                settled.availableBalance(),
                settled.reservedBalance(),
                LocalDateTime.now()
        ));
    }

    private void processManualCommand(AdjustCommand command,
                                      MovementType movementType,
                                      String fallbackDescription,
                                      WalletOperator operator) {
        String owner = validateOwner(command.owner());
        BigDecimal amount = positive(command.amount(), "Amount");
        String referenceId = validateReference(command.referenceId());

        // Guard clause para idempotencia ante reintentos.
        if (ledgerEntryRepository.existsByOwnerAndReferenceIdAndMovementType(owner, referenceId, movementType)) {
            return;
        }

        // Bloqueo del wallet durante el ajuste.
        WalletAccount account = lockOrOpenAccount(owner);
        // Aplicación de la regla de negocio (deposit/withdraw/fee) en el dominio.
        WalletAccount updated = operator.apply(account);
        WalletAccount persisted = walletAccountRepository.save(updated);

        // Deltas calculados desde estado anterior -> posterior para registrar en el ledger.
        BigDecimal availableDelta = persisted.availableBalance().subtract(account.availableBalance());
        BigDecimal reservedDelta = persisted.reservedBalance().subtract(account.reservedBalance());

        ledgerEntryRepository.save(WalletTransaction.newEntry(
                owner,
                movementType,
                amount,
                availableDelta,
                reservedDelta,
                persisted.availableBalance(),
                persisted.reservedBalance(),
                ReferenceType.MANUAL,
                referenceId,
                command.description() == null || command.description().isBlank() ? fallbackDescription : command.description(),
                LocalDateTime.now()
        ));
    }

    private WalletAccount lockOrOpenAccount(String owner) {
        // Si no existe aún en la base de datos, abrimos una cuenta nueva.
        return walletAccountRepository.findByOwnerForUpdate(owner).orElseGet(() -> WalletAccount.open(owner));
    }

    private void publishInsufficientFunds(String orderRef,
                                          String owner,
                                          BigDecimal requestedAmount,
                                          WalletAccount account,
                                          String reason) {
        // Publicamos un evento en lugar de lanzar excepción para que el sistema pueda reaccionar (p. ej. rechazar orden).
        eventPublisher.publish(new InsufficientFundsEvent(
                orderRef,
                owner,
                requestedAmount,
                account.availableBalance(),
                account.reservedBalance(),
                reason,
                LocalDateTime.now()
        ));
    }

    private void publishAvailableCashUpdated(String owner,
                                             BigDecimal availableCash,
                                             String trigger,
                                             String referenceId,
                                             LocalDateTime occurredAt) {
        eventPublisher.publish(new AvailableCashUpdatedEvent(
                owner,
                availableCash,
                trigger,
                referenceId,
                occurredAt
        ));
    }

    private static BigDecimal orderAmount(BigDecimal quantity, BigDecimal price) {
        return positive(quantity, "Quantity").multiply(positive(price, "Price"));
    }

    private static BigDecimal positive(BigDecimal value, String field) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletDomainException(field + " must be greater than zero");
        }
        return value;
    }

    private static String validateOwner(String owner) {
        if (owner == null || owner.isBlank()) {
            throw new WalletDomainException("Owner is required");
        }
        return owner;
    }

    private static String validateReference(String reference) {
        if (reference == null || reference.isBlank()) {
            throw new WalletDomainException("Reference id is required");
        }
        return reference;
    }


    private static LocalDateTime now(LocalDateTime occurredAt) {
        return occurredAt == null ? LocalDateTime.now() : occurredAt;
    }

    private static Side parseSide(String value) {
        if (value == null || value.isBlank()) {
            throw new WalletDomainException("Order side is required");
        }
        try {
            return Side.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new WalletDomainException("Unsupported order side: " + value);
        }
    }

    private enum Side {
        BUY,
        SELL
    }

    @FunctionalInterface
    private interface WalletOperator {
        WalletAccount apply(WalletAccount account);
    }
}

