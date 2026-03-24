package com.trading.platform.eztrade.wallet.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalletAccountTest {

    @Test
    @DisplayName("reserve mueve saldo de disponible a reservado")
    void reserve_moves_balance_to_reserved() {
        WalletAccount account = WalletAccount.rehydrate("user@demo.com", new BigDecimal("1000"), BigDecimal.ZERO);

        WalletAccount updated = account.reserve(new BigDecimal("250"));

        assertThat(updated.availableBalance()).isEqualByComparingTo("750");
        assertThat(updated.reservedBalance()).isEqualByComparingTo("250");
    }

    @Test
    @DisplayName("release devuelve saldo reservado a disponible")
    void release_restores_reserved_balance() {
        WalletAccount account = WalletAccount.rehydrate("user@demo.com", new BigDecimal("600"), new BigDecimal("400"));

        WalletAccount updated = account.release(new BigDecimal("150"));

        assertThat(updated.availableBalance()).isEqualByComparingTo("750");
        assertThat(updated.reservedBalance()).isEqualByComparingTo("250");
    }

    @Test
    @DisplayName("withdraw falla cuando no hay saldo disponible")
    void withdraw_fails_with_insufficient_available() {
        WalletAccount account = WalletAccount.rehydrate("user@demo.com", new BigDecimal("50"), BigDecimal.ZERO);

        assertThatThrownBy(() -> account.withdraw(new BigDecimal("60")))
                .isInstanceOf(WalletDomainException.class)
                .hasMessageContaining("Insufficient available funds");
    }
}

