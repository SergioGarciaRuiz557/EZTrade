package com.trading.platform.eztrade.wallet.application.services;

import com.trading.platform.eztrade.trading.domain.events.OrderCancelledEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderPlacedEvent;
import com.trading.platform.eztrade.wallet.application.ports.in.AdjustWalletFundsUseCase;
import com.trading.platform.eztrade.wallet.application.ports.in.HandleOrderCancelledUseCase;
import com.trading.platform.eztrade.wallet.application.ports.in.HandleOrderExecutedUseCase;
import com.trading.platform.eztrade.wallet.application.ports.in.HandleOrderPlacedUseCase;
import com.trading.platform.eztrade.wallet.application.ports.out.DomainEventPublisherPort;
import com.trading.platform.eztrade.wallet.application.ports.out.LedgerEntryRepositoryPort;
import com.trading.platform.eztrade.wallet.application.ports.out.WalletAccountRepositoryPort;
import com.trading.platform.eztrade.wallet.domain.LedgerEntry;
import com.trading.platform.eztrade.wallet.domain.MovementType;
import com.trading.platform.eztrade.wallet.domain.ReferenceType;
import com.trading.platform.eztrade.wallet.domain.WalletAccount;
import com.trading.platform.eztrade.wallet.domain.WalletDomainException;
import com.trading.platform.eztrade.wallet.domain.events.FundsReleasedEvent;
import com.trading.platform.eztrade.wallet.domain.events.FundsReservedEvent;
import com.trading.platform.eztrade.wallet.domain.events.FundsSettledEvent;
import com.trading.platform.eztrade.wallet.domain.events.InsufficientFundsEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@Transactional
public class WalletService implements HandleOrderPlacedUseCase,
        HandleOrderCancelledUseCase,
        HandleOrderExecutedUseCase,
        AdjustWalletFundsUseCase {

    private static final String ORDER_SETTLEMENT_DEBIT = "DEBIT";
    private static final String ORDER_SETTLEMENT_CREDIT = "CREDIT";

    private final WalletAccountRepositoryPort walletAccountRepository;
    private final LedgerEntryRepositoryPort ledgerEntryRepository;
    private final DomainEventPublisherPort eventPublisher;

    public WalletService(WalletAccountRepositoryPort walletAccountRepository,
                         LedgerEntryRepositoryPort ledgerEntryRepository,
                         DomainEventPublisherPort eventPublisher) {
        this.walletAccountRepository = walletAccountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void handle(OrderPlacedEvent event) {
        if (!"BUY".equalsIgnoreCase(event.side())) {
            return;
        }

        String owner = validateOwner(event.owner());
        String orderRef = String.valueOf(event.orderId());
        BigDecimal amount = orderAmount(event.quantity(), event.price());

        if (ledgerEntryRepository.existsByOwnerAndReferenceIdAndMovementType(owner, orderRef, MovementType.RESERVE)) {
            return;
        }

        WalletAccount account = lockOrOpenAccount(owner);
        if (account.availableBalance().compareTo(amount) < 0) {
            publishInsufficientFunds(orderRef, owner, amount, account, "Available balance is not enough to reserve funds");
            return;
        }

        WalletAccount updated = walletAccountRepository.save(account.reserve(amount));

        ledgerEntryRepository.save(LedgerEntry.newEntry(
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

        eventPublisher.publish(new FundsReservedEvent(
                orderRef,
                owner,
                amount,
                updated.availableBalance(),
                updated.reservedBalance(),
                LocalDateTime.now()
        ));
    }

    @Override
    public void handle(OrderCancelledEvent event) {
        String owner = validateOwner(event.owner());
        String orderRef = String.valueOf(event.orderId());

        if (ledgerEntryRepository.existsByOwnerAndReferenceIdAndMovementType(owner, orderRef, MovementType.RELEASE)) {
            return;
        }

        LedgerEntry reserveEntry = ledgerEntryRepository
                .findByOwnerAndReferenceIdAndMovementType(owner, orderRef, MovementType.RESERVE)
                .orElse(null);

        if (reserveEntry == null) {
            return;
        }

        if (ledgerEntryRepository.existsByOwnerAndReferenceIdAndMovementType(owner, orderRef, MovementType.SETTLEMENT_DEBIT)) {
            return;
        }

        WalletAccount account = lockOrOpenAccount(owner);
        BigDecimal amountToRelease = reserveEntry.amount();

        WalletAccount updated = walletAccountRepository.save(account.release(amountToRelease));

        ledgerEntryRepository.save(LedgerEntry.newEntry(
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
        Side side = parseSide(event.side());
        switch (side) {
            case BUY -> settleBuy(event);
            case SELL -> settleSell(event);
        }
    }

    @Override
    public void deposit(AdjustCommand command) {
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

    private void settleBuy(OrderExecutedEvent event) {
        String owner = validateOwner(event.owner());
        String orderRef = String.valueOf(event.orderId());
        BigDecimal amount = orderAmount(event.quantity(), event.price());

        if (ledgerEntryRepository.existsByOwnerAndReferenceIdAndMovementType(owner, orderRef, MovementType.SETTLEMENT_DEBIT)) {
            return;
        }

        LedgerEntry reserveEntry = ledgerEntryRepository
                .findByOwnerAndReferenceIdAndMovementType(owner, orderRef, MovementType.RESERVE)
                .orElse(null);

        if (reserveEntry == null || reserveEntry.amount().compareTo(amount) < 0) {
            WalletAccount account = lockOrOpenAccount(owner);
            publishInsufficientFunds(orderRef, owner, amount, account, "Reserved funds are not enough to settle buy order");
            return;
        }

        WalletAccount account = lockOrOpenAccount(owner);
        WalletAccount workingAccount = account;

        BigDecimal releaseDelta = reserveEntry.amount().subtract(amount);
        if (releaseDelta.compareTo(BigDecimal.ZERO) > 0
                && !ledgerEntryRepository.existsByOwnerAndReferenceIdAndMovementType(owner, orderRef, MovementType.RELEASE)) {
            workingAccount = walletAccountRepository.save(workingAccount.release(releaseDelta));
            ledgerEntryRepository.save(LedgerEntry.newEntry(
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

        ledgerEntryRepository.save(LedgerEntry.newEntry(
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

        if (ledgerEntryRepository.existsByOwnerAndReferenceIdAndMovementType(owner, orderRef, MovementType.SETTLEMENT_CREDIT)) {
            return;
        }

        WalletAccount account = lockOrOpenAccount(owner);
        WalletAccount settled = walletAccountRepository.save(account.settleCredit(amount));

        ledgerEntryRepository.save(LedgerEntry.newEntry(
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

        if (ledgerEntryRepository.existsByOwnerAndReferenceIdAndMovementType(owner, referenceId, movementType)) {
            return;
        }

        WalletAccount account = lockOrOpenAccount(owner);
        WalletAccount updated = operator.apply(account);
        WalletAccount persisted = walletAccountRepository.save(updated);

        BigDecimal availableDelta = persisted.availableBalance().subtract(account.availableBalance());
        BigDecimal reservedDelta = persisted.reservedBalance().subtract(account.reservedBalance());

        ledgerEntryRepository.save(LedgerEntry.newEntry(
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
        return walletAccountRepository.findByOwnerForUpdate(owner).orElseGet(() -> WalletAccount.open(owner));
    }

    private void publishInsufficientFunds(String orderRef,
                                          String owner,
                                          BigDecimal requestedAmount,
                                          WalletAccount account,
                                          String reason) {
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

