package com.trading.platform.eztrade.wallet.adapter.in.web;

import com.trading.platform.eztrade.security.configuration.BeansConfig;
import com.trading.platform.eztrade.user.api.UserOwnerLookupPort;
import com.trading.platform.eztrade.wallet.application.ports.in.AdjustWalletFundsUseCase;
import com.trading.platform.eztrade.wallet.application.ports.in.GetWalletBalanceUseCase;
import com.trading.platform.eztrade.wallet.application.ports.in.GetWalletTransactionsUseCase;
import com.trading.platform.eztrade.wallet.application.ports.in.TransferWalletFundsUseCase;
import com.trading.platform.eztrade.wallet.domain.MovementType;
import com.trading.platform.eztrade.wallet.domain.ReferenceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({WalletControllerTest.TestConfig.class, WalletExceptionHandler.class})
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdjustWalletFundsUseCase adjustWalletFundsUseCase;

    @Autowired
    private GetWalletBalanceUseCase getWalletBalanceUseCase;

    @Autowired
    private TransferWalletFundsUseCase transferWalletFundsUseCase;

    @Autowired
    private GetWalletTransactionsUseCase getWalletTransactionsUseCase;

    @Autowired
    private UserOwnerLookupPort userOwnerLookupPort;

    private static Authentication auth(String owner) {
        return new UsernamePasswordAuthenticationToken(owner, null);
    }

    @Test
    @DisplayName("POST /api/v1/wallet/deposit deposita y devuelve balances")
    void deposit_returnsCreatedAndBalance() throws Exception {
        given(getWalletBalanceUseCase.getBalance("demo@example.com"))
                .willReturn(new GetWalletBalanceUseCase.BalanceView(new BigDecimal("1000.00"), BigDecimal.ZERO));

        mockMvc.perform(post("/api/v1/wallet/deposit")
                        .principal(auth("demo@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":1000.00,\"referenceId\":\"dep-1\",\"description\":\"Initial funding\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.owner").value("demo@example.com"))
                .andExpect(jsonPath("$.availableBalance").value(1000.00))
                .andExpect(jsonPath("$.reservedBalance").value(0));

        verify(adjustWalletFundsUseCase).deposit(any(AdjustWalletFundsUseCase.AdjustCommand.class));
    }

    @Test
    @DisplayName("GET /api/v1/wallet/balance devuelve el balance del usuario autenticado")
    void getBalance_returnsOk() throws Exception {
        given(getWalletBalanceUseCase.getBalance("demo@example.com"))
                .willReturn(new GetWalletBalanceUseCase.BalanceView(new BigDecimal("700.00"), new BigDecimal("300.00")));

        mockMvc.perform(get("/api/v1/wallet/balance")
                        .principal(auth("demo@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner").value("demo@example.com"))
                .andExpect(jsonPath("$.availableBalance").value(700.00))
                .andExpect(jsonPath("$.reservedBalance").value(300.00));
    }

    @Test
    @DisplayName("POST /api/v1/wallet/withdraw retira y devuelve balances")
    void withdraw_returnsOkAndBalance() throws Exception {
        given(getWalletBalanceUseCase.getBalance("demo@example.com"))
                .willReturn(new GetWalletBalanceUseCase.BalanceView(new BigDecimal("750.00"), BigDecimal.ZERO));

        mockMvc.perform(post("/api/v1/wallet/withdraw")
                        .principal(auth("demo@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":250.00,\"referenceId\":\"wd-1\",\"description\":\"Cash out\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner").value("demo@example.com"))
                .andExpect(jsonPath("$.availableBalance").value(750.00))
                .andExpect(jsonPath("$.reservedBalance").value(0));

        verify(adjustWalletFundsUseCase).withdraw(any(AdjustWalletFundsUseCase.AdjustCommand.class));
    }

    @Test
    @DisplayName("POST /api/v1/wallet/transfer valida destinatario y devuelve balance")
    void transfer_returnsOkAndBalance() throws Exception {
        given(userOwnerLookupPort.findOwner("recipient@example.com"))
                .willReturn(Optional.of("recipient@example.com"));
        given(getWalletBalanceUseCase.getBalance("demo@example.com"))
                .willReturn(new GetWalletBalanceUseCase.BalanceView(new BigDecimal("400.00"), BigDecimal.ZERO));

        mockMvc.perform(post("/api/v1/wallet/transfer")
                        .principal(auth("demo@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipient\":\"recipient@example.com\",\"amount\":100.00,\"referenceId\":\"tr-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner").value("demo@example.com"))
                .andExpect(jsonPath("$.availableBalance").value(400.00));

        verify(transferWalletFundsUseCase).transfer(any(TransferWalletFundsUseCase.TransferCommand.class));
    }

    @Test
    @DisplayName("GET /api/v1/wallet/transactions devuelve historial")
    void getTransactions_returnsOk() throws Exception {
        given(getWalletTransactionsUseCase.getTransactions("demo@example.com"))
                .willReturn(List.of(new GetWalletTransactionsUseCase.TransactionView(
                        1L,
                        MovementType.DEPOSIT,
                        new BigDecimal("1000.00"),
                        new BigDecimal("1000.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("1000.00"),
                        BigDecimal.ZERO,
                        ReferenceType.MANUAL,
                        "dep-1",
                        "Initial funding",
                        LocalDateTime.of(2026, 5, 8, 10, 0)
                )));

        mockMvc.perform(get("/api/v1/wallet/transactions")
                        .principal(auth("demo@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].movementType").value("DEPOSIT"))
                .andExpect(jsonPath("$[0].amount").value(1000.00))
                .andExpect(jsonPath("$[0].referenceId").value("dep-1"));
    }

    @Test
    @DisplayName("POST /api/v1/wallet/deposit con amount invalido devuelve 400")
    void deposit_withInvalidAmount_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/wallet/deposit")
                        .principal(auth("demo@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":0}"))
                .andExpect(status().isBadRequest());
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        AdjustWalletFundsUseCase adjustWalletFundsUseCase() {
            return Mockito.mock(AdjustWalletFundsUseCase.class);
        }

        @Bean
        GetWalletBalanceUseCase getWalletBalanceUseCase() {
            return Mockito.mock(GetWalletBalanceUseCase.class);
        }

        @Bean
        TransferWalletFundsUseCase transferWalletFundsUseCase() {
            return Mockito.mock(TransferWalletFundsUseCase.class);
        }

        @Bean
        GetWalletTransactionsUseCase getWalletTransactionsUseCase() {
            return Mockito.mock(GetWalletTransactionsUseCase.class);
        }

        @Bean
        UserOwnerLookupPort userOwnerLookupPort() {
            return Mockito.mock(UserOwnerLookupPort.class);
        }

        @Bean
        BeansConfig.SecurityPermissionEvaluator securityPermissionEvaluator() {
            return Mockito.mock(BeansConfig.SecurityPermissionEvaluator.class);
        }
    }
}

