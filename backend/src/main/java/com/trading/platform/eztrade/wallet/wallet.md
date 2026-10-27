# Wallet Module

## Purpose

`wallet` is the source of truth for each user's cash. It maintains available balance, reserved balance, and an auditable movement ledger.

## Responsibilities

- Create or rebuild cash accounts per user.
- Apply deposits, withdrawals, fees, and transfers.
- Reserve funds for BUY orders.
- Release funds when orders are cancelled.
- Settle buys and sells when trading requests execution.
- Publish balance and funds events.
- Register each movement in `WalletTransaction`.

## Domain

### `WalletAccount`

Immutable entity with two balances:

- `availableBalance`: free cash that can be withdrawn, transferred, or reserved.
- `reservedBalance`: cash held for pending BUY orders waiting for settlement.

Main operations:

- `deposit(amount)`: increases available balance.
- `withdraw(amount)`: reduces available balance if there are enough funds.
- `reserve(amount)`: moves cash from available to reserved.
- `release(amount)`: returns reserved cash to available.
- `settleReservedDebit(amount)`: consumes reserved cash in an executed buy.
- `settleCredit(amount)`: credits available cash in an executed sell.
- `chargeFee(amount)`: applies a fee as a withdrawal.

### `WalletTransaction`

Immutable ledger entry. It stores:

- owner,
- movement type,
- base amount,
- delta on available balance,
- delta on reserved balance,
- final balances,
- reference type and id,
- description,
- occurrence date.

### Enums

`MovementType` classifies movements such as `DEPOSIT`, `WITHDRAWAL`, `TRANSFER_OUT`, `TRANSFER_IN`, `RESERVE`, `RELEASE`, `SETTLEMENT_DEBIT`, `SETTLEMENT_CREDIT`, and `FEE`.

`ReferenceType` indicates whether the movement comes from an order or from a manual adjustment.

## Application Service

`WalletService` implements:

- `HandleOrderPlacedUseCase`
- `HandleOrderCancelledUseCase`
- `HandleOrderExecutedUseCase`
- `AdjustWalletFundsUseCase`
- `TransferWalletFundsUseCase`
- `GetWalletBalanceUseCase`
- `GetWalletTransactionsUseCase`

The service is annotated with `@Transactional` so the account and ledger are updated atomically.

## REST API

Base: `/api/v1/wallet`

| Method | Route | Use |
|---|---|---|
| `POST` | `/deposit` | Deposits manual funds. |
| `POST` | `/withdraw` | Withdraws available funds. |
| `POST` | `/transfer` | Transfers funds between users. |
| `GET` | `/balance` | Queries available and reserved balances. |
| `GET` | `/transactions` | Queries the user's ledger. |

## Events Consumed from Trading

- `OrderPlacedEvent`: if it is BUY, reserves `quantity * price`.
- `OrderCancelledEvent`: releases reservation if it existed and was not settled.
- `OrderExecutionRequestEvent`: settles BUY or SELL. Then publishes `OrderExecutedEvent`.

## Events Published

- `FundsReservedEvent`: reservation applied.
- `FundsReleasedEvent`: reservation released.
- `FundsSettledEvent`: debit/credit settlement applied.
- `InsufficientFundsEvent`: insufficient funds.
- `AvailableCashUpdatedEvent`: available balance updated so portfolio can synchronize its projection.
- `OrderExecutedEvent`: confirmed execution for portfolio and notifications.

## Idempotency and Concurrency

Wallet avoids duplicate movements by checking `(owner, referenceId, movementType)` before applying an operation. To modify accounts, it uses `findByOwnerForUpdate`, so two concurrent operations for the same user do not calculate balances over a stale snapshot.

## BUY Flow

1. Trading publishes `OrderPlacedEvent`.
2. Wallet reserves the order amount and records `RESERVE`.
3. Trading publishes `OrderExecutionRequestEvent`.
4. Wallet checks that enough reservation exists.
5. Wallet consumes reserved balance with `SETTLEMENT_DEBIT`.
6. Wallet publishes `OrderExecutedEvent` and `AvailableCashUpdatedEvent`.

## SELL Flow

1. Trading publishes `OrderExecutionRequestEvent`.
2. Wallet credits the amount to available balance with `SETTLEMENT_CREDIT`.
3. Wallet publishes `OrderExecutedEvent` and `AvailableCashUpdatedEvent`.

## Transfers

`transfer(...)` loads both accounts in deterministic order to reduce deadlock risk, withdraws from the sender, deposits to the recipient, and writes two ledger entries: `TRANSFER_OUT` and `TRANSFER_IN`.
