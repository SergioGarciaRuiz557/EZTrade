package com.trading.platform.eztrade.wallet.adapter.in.web;

import com.trading.platform.eztrade.wallet.adapter.in.web.dto.WalletBalanceResponse;
import com.trading.platform.eztrade.wallet.adapter.in.web.dto.WalletDepositRequest;
import com.trading.platform.eztrade.wallet.application.ports.in.AdjustWalletFundsUseCase;
import com.trading.platform.eztrade.wallet.application.ports.in.GetWalletBalanceUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * API REST del modulo wallet para consultar balance y depositar fondos.
 */
@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private final AdjustWalletFundsUseCase adjustWalletFundsUseCase;
    private final GetWalletBalanceUseCase getWalletBalanceUseCase;

    public WalletController(AdjustWalletFundsUseCase adjustWalletFundsUseCase,
                            GetWalletBalanceUseCase getWalletBalanceUseCase) {
        this.adjustWalletFundsUseCase = adjustWalletFundsUseCase;
        this.getWalletBalanceUseCase = getWalletBalanceUseCase;
    }

    @PostMapping("/deposit")
    public ResponseEntity<WalletBalanceResponse> deposit(@RequestBody @Valid WalletDepositRequest request,
                                                         Authentication authentication) {
        String owner = authentication.getName();
        String referenceId = (request.referenceId() == null || request.referenceId().isBlank())
                ? UUID.randomUUID().toString()
                : request.referenceId();

        adjustWalletFundsUseCase.deposit(new AdjustWalletFundsUseCase.AdjustCommand(
                owner,
                request.amount(),
                referenceId,
                request.description()
        ));

        GetWalletBalanceUseCase.BalanceView balance = getWalletBalanceUseCase.getBalance(owner);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new WalletBalanceResponse(owner, balance.availableBalance(), balance.reservedBalance()));
    }

    @GetMapping("/balance")
    public ResponseEntity<WalletBalanceResponse> getBalance(Authentication authentication) {
        String owner = authentication.getName();
        GetWalletBalanceUseCase.BalanceView balance = getWalletBalanceUseCase.getBalance(owner);
        return ResponseEntity.ok(new WalletBalanceResponse(owner, balance.availableBalance(), balance.reservedBalance()));
    }
}

