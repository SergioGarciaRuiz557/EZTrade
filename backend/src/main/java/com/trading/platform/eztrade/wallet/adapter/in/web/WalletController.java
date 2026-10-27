package com.trading.platform.eztrade.wallet.adapter.in.web;

import com.trading.platform.eztrade.user.api.UserOwnerLookupPort;
import com.trading.platform.eztrade.wallet.adapter.in.web.dto.WalletBalanceResponse;
import com.trading.platform.eztrade.wallet.adapter.in.web.dto.WalletDepositRequest;
import com.trading.platform.eztrade.wallet.adapter.in.web.dto.WalletTransactionResponse;
import com.trading.platform.eztrade.wallet.adapter.in.web.dto.WalletTransferRequest;
import com.trading.platform.eztrade.wallet.adapter.in.web.dto.WalletWithdrawalRequest;
import com.trading.platform.eztrade.wallet.application.ports.in.AdjustWalletFundsUseCase;
import com.trading.platform.eztrade.wallet.application.ports.in.GetWalletBalanceUseCase;
import com.trading.platform.eztrade.wallet.application.ports.in.GetWalletTransactionsUseCase;
import com.trading.platform.eztrade.wallet.application.ports.in.TransferWalletFundsUseCase;
import com.trading.platform.eztrade.wallet.domain.WalletDomainException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Wallet module REST API for balances, deposits, withdrawals, transfers, and movement history.
 */
@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private final AdjustWalletFundsUseCase adjustWalletFundsUseCase;
    private final GetWalletBalanceUseCase getWalletBalanceUseCase;
    private final TransferWalletFundsUseCase transferWalletFundsUseCase;
    private final GetWalletTransactionsUseCase getWalletTransactionsUseCase;
    private final UserOwnerLookupPort userOwnerLookupPort;

    public WalletController(AdjustWalletFundsUseCase adjustWalletFundsUseCase,
                            GetWalletBalanceUseCase getWalletBalanceUseCase,
                            TransferWalletFundsUseCase transferWalletFundsUseCase,
                            GetWalletTransactionsUseCase getWalletTransactionsUseCase,
                            UserOwnerLookupPort userOwnerLookupPort) {
        this.adjustWalletFundsUseCase = adjustWalletFundsUseCase;
        this.getWalletBalanceUseCase = getWalletBalanceUseCase;
        this.transferWalletFundsUseCase = transferWalletFundsUseCase;
        this.getWalletTransactionsUseCase = getWalletTransactionsUseCase;
        this.userOwnerLookupPort = userOwnerLookupPort;
    }

    @PostMapping("/deposit")
    public ResponseEntity<WalletBalanceResponse> deposit(@RequestBody @Valid WalletDepositRequest request,
                                                         Authentication authentication) {
        String owner = authentication.getName();
        String referenceId = referenceId(request.referenceId());

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

    @PostMapping("/withdraw")
    public ResponseEntity<WalletBalanceResponse> withdraw(@RequestBody @Valid WalletWithdrawalRequest request,
                                                          Authentication authentication) {
        String owner = authentication.getName();
        adjustWalletFundsUseCase.withdraw(new AdjustWalletFundsUseCase.AdjustCommand(
                owner,
                request.amount(),
                referenceId(request.referenceId()),
                request.description()
        ));

        return ResponseEntity.ok(balanceResponse(owner));
    }

    @PostMapping("/transfer")
    public ResponseEntity<WalletBalanceResponse> transfer(@RequestBody @Valid WalletTransferRequest request,
                                                          Authentication authentication) {
        String owner = authentication.getName();
        String recipientOwner = userOwnerLookupPort.findOwner(request.recipient())
                .orElseThrow(() -> new WalletDomainException("Recipient user not found"));

        transferWalletFundsUseCase.transfer(new TransferWalletFundsUseCase.TransferCommand(
                owner,
                recipientOwner,
                request.amount(),
                referenceId(request.referenceId()),
                request.description()
        ));

        return ResponseEntity.ok(balanceResponse(owner));
    }

    @GetMapping("/balance")
    public ResponseEntity<WalletBalanceResponse> getBalance(Authentication authentication) {
        String owner = authentication.getName();
        GetWalletBalanceUseCase.BalanceView balance = getWalletBalanceUseCase.getBalance(owner);
        return ResponseEntity.ok(new WalletBalanceResponse(owner, balance.availableBalance(), balance.reservedBalance()));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<WalletTransactionResponse>> getTransactions(Authentication authentication) {
        String owner = authentication.getName();
        List<WalletTransactionResponse> response = getWalletTransactionsUseCase.getTransactions(owner).stream()
                .map(WalletController::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    private WalletBalanceResponse balanceResponse(String owner) {
        GetWalletBalanceUseCase.BalanceView balance = getWalletBalanceUseCase.getBalance(owner);
        return new WalletBalanceResponse(owner, balance.availableBalance(), balance.reservedBalance());
    }

    private static WalletTransactionResponse toResponse(GetWalletTransactionsUseCase.TransactionView transaction) {
        return new WalletTransactionResponse(
                transaction.id(),
                transaction.movementType(),
                transaction.amount(),
                transaction.availableDelta(),
                transaction.reservedDelta(),
                transaction.availableBalanceAfter(),
                transaction.reservedBalanceAfter(),
                transaction.referenceType(),
                transaction.referenceId(),
                transaction.description(),
                transaction.occurredAt()
        );
    }

    private static String referenceId(String requestedReferenceId) {
        return requestedReferenceId == null || requestedReferenceId.isBlank()
                ? UUID.randomUUID().toString()
                : requestedReferenceId;
    }
}

