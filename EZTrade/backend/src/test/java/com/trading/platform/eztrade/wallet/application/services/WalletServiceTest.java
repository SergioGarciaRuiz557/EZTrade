package com.trading.platform.eztrade.wallet.application.services;

import com.trading.platform.eztrade.trading.domain.events.OrderCancelledEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderPlacedEvent;
import com.trading.platform.eztrade.wallet.application.ports.out.DomainEventPublisherPort;
import com.trading.platform.eztrade.wallet.application.ports.out.WalletTransactionRepositoryPort;
import com.trading.platform.eztrade.wallet.application.ports.out.WalletAccountRepositoryPort;
import com.trading.platform.eztrade.wallet.domain.WalletTransaction;
import com.trading.platform.eztrade.wallet.domain.MovementType;
import com.trading.platform.eztrade.wallet.domain.ReferenceType;
import com.trading.platform.eztrade.wallet.domain.WalletAccount;
import com.trading.platform.eztrade.wallet.domain.events.AvailableCashUpdatedEvent;
import com.trading.platform.eztrade.wallet.domain.events.FundsReleasedEvent;
import com.trading.platform.eztrade.wallet.domain.events.FundsReservedEvent;
import com.trading.platform.eztrade.wallet.domain.events.FundsSettledEvent;
import com.trading.platform.eztrade.wallet.domain.events.InsufficientFundsEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletAccountRepositoryPort walletAccountRepository;

    @Mock
    private WalletTransactionRepositoryPort ledgerEntryRepository;

    @Mock
    private DomainEventPublisherPort eventPublisher;

    @InjectMocks
    private WalletService walletService;

    @Test
    @DisplayName("OrderPlaced BUY reserva fondos y publica FundsReservedEvent")
    void placed_buy_reserves_funds() {
        OrderPlacedEvent event = new OrderPlacedEvent(
                1L,
                "user@demo.com",
                "IBM",
                "BUY",
                new BigDecimal("2"),
                new BigDecimal("100"),
                LocalDateTime.now()
        );

        given(ledgerEntryRepository.existsByOwnerAndReferenceIdAndMovementType("user@demo.com", "1", MovementType.RESERVE))
                .willReturn(false);
        given(walletAccountRepository.findByOwnerForUpdate("user@demo.com"))
                .willReturn(Optional.of(WalletAccount.rehydrate("user@demo.com", new BigDecimal("500"), BigDecimal.ZERO)));
        given(walletAccountRepository.save(any(WalletAccount.class))).willAnswer(i -> i.getArgument(0));
        given(ledgerEntryRepository.save(any(WalletTransaction.class))).willAnswer(i -> i.getArgument(0));

        walletService.handle(event);

        ArgumentCaptor<WalletAccount> accountCaptor = ArgumentCaptor.forClass(WalletAccount.class);
        verify(walletAccountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().availableBalance()).isEqualByComparingTo("300");
        assertThat(accountCaptor.getValue().reservedBalance()).isEqualByComparingTo("200");

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, atLeastOnce()).publish(eventCaptor.capture());
        assertThat(eventCaptor.getAllValues()).anyMatch(FundsReservedEvent.class::isInstance);
        assertThat(eventCaptor.getAllValues()).anyMatch(AvailableCashUpdatedEvent.class::isInstance);
    }

    @Test
    @DisplayName("OrderPlaced BUY sin saldo publica InsufficientFundsEvent")
    void placed_buy_without_balance_publishes_insufficient_event() {
        OrderPlacedEvent event = new OrderPlacedEvent(
                2L,
                "user@demo.com",
                "IBM",
                "BUY",
                new BigDecimal("2"),
                new BigDecimal("100"),
                LocalDateTime.now()
        );

        given(ledgerEntryRepository.existsByOwnerAndReferenceIdAndMovementType("user@demo.com", "2", MovementType.RESERVE))
                .willReturn(false);
        given(walletAccountRepository.findByOwnerForUpdate("user@demo.com"))
                .willReturn(Optional.of(WalletAccount.rehydrate("user@demo.com", new BigDecimal("20"), BigDecimal.ZERO)));

        walletService.handle(event);

        verify(walletAccountRepository, never()).save(any(WalletAccount.class));
        verify(ledgerEntryRepository, never()).save(any(WalletTransaction.class));
        verify(eventPublisher).publish(any(InsufficientFundsEvent.class));
    }

    @Test
    @DisplayName("OrderCancelled libera fondos reservados")
    void cancelled_order_releases_funds() {
        OrderCancelledEvent event = new OrderCancelledEvent(3L, "user@demo.com", "IBM", LocalDateTime.now());

        WalletTransaction reserveEntry = WalletTransaction.newEntry(
                "user@demo.com",
                MovementType.RESERVE,
                new BigDecimal("120"),
                new BigDecimal("-120"),
                new BigDecimal("120"),
                new BigDecimal("380"),
                new BigDecimal("120"),
                ReferenceType.ORDER,
                "3",
                "reserve",
                LocalDateTime.now()
        );

        given(ledgerEntryRepository.existsByOwnerAndReferenceIdAndMovementType("user@demo.com", "3", MovementType.RELEASE))
                .willReturn(false);
        given(ledgerEntryRepository.existsByOwnerAndReferenceIdAndMovementType("user@demo.com", "3", MovementType.SETTLEMENT_DEBIT))
                .willReturn(false);
        given(ledgerEntryRepository.findByOwnerAndReferenceIdAndMovementType("user@demo.com", "3", MovementType.RESERVE))
                .willReturn(Optional.of(reserveEntry));
        given(walletAccountRepository.findByOwnerForUpdate("user@demo.com"))
                .willReturn(Optional.of(WalletAccount.rehydrate("user@demo.com", new BigDecimal("380"), new BigDecimal("120"))));
        given(walletAccountRepository.save(any(WalletAccount.class))).willAnswer(i -> i.getArgument(0));
        given(ledgerEntryRepository.save(any(WalletTransaction.class))).willAnswer(i -> i.getArgument(0));

        walletService.handle(event);

        verify(walletAccountRepository, times(1)).save(any(WalletAccount.class));
        verify(eventPublisher, atLeastOnce()).publish(any(FundsReleasedEvent.class));
    }

    @Test
    @DisplayName("OrderExecuted SELL liquida abono en disponible")
    void executed_sell_settles_credit() {
        OrderExecutedEvent event = new OrderExecutedEvent(
                20L,
                "user@demo.com",
                "IBM",
                "SELL",
                new BigDecimal("1"),
                new BigDecimal("150"),
                LocalDateTime.now()
        );

        given(ledgerEntryRepository.existsByOwnerAndReferenceIdAndMovementType("user@demo.com", "20", MovementType.SETTLEMENT_CREDIT))
                .willReturn(false);
        given(walletAccountRepository.findByOwnerForUpdate("user@demo.com"))
                .willReturn(Optional.of(WalletAccount.rehydrate("user@demo.com", new BigDecimal("300"), BigDecimal.ZERO)));
        given(walletAccountRepository.save(any(WalletAccount.class))).willAnswer(i -> i.getArgument(0));
        given(ledgerEntryRepository.save(any(WalletTransaction.class))).willAnswer(i -> i.getArgument(0));

        walletService.handle(event);

        ArgumentCaptor<WalletAccount> accountCaptor = ArgumentCaptor.forClass(WalletAccount.class);
        verify(walletAccountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().availableBalance()).isEqualByComparingTo("450");

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, atLeastOnce()).publish(eventCaptor.capture());
        assertThat(eventCaptor.getAllValues()).anyMatch(FundsSettledEvent.class::isInstance);
        assertThat(eventCaptor.getAllValues()).anyMatch(AvailableCashUpdatedEvent.class::isInstance);
    }
}

