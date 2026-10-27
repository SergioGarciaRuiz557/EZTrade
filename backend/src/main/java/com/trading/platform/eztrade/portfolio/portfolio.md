# Portfolio Module

## Purpose

`portfolio` maintains each user's portfolio:

- open positions by symbol,
- weighted average cost,
- realized PnL,
- available cash projected from wallet,
- current market valuations for REST queries.

The module is declared with allowed dependencies on `trading :: events`, `wallet :: events`, and `market :: api`.

## Boundaries

Portfolio does not execute orders and does not move real money. Trading decides the order lifecycle, and wallet is the source of truth for cash. Portfolio consumes events to maintain a consistent read view.

## Structure

```text
portfolio/
  domain/
    Position, CashProjection, PortfolioSnapshot, PositionMarketValuation
    events/
      PositionOpenedEvent, PositionIncreasedEvent, PositionReducedEvent
      PositionClosedEvent, PortfolioValuationUpdatedEvent
  application/
    services/PortfolioService
    ports/in/
      HandleOrderExecutedUseCase, HandleWalletCashUpdatedUseCase, GetPortfolioUseCase
    ports/out/
      PositionRepositoryPort, CashProjectionRepositoryPort, DomainEventPublisherPort
  adapter/
    in/events/
      TradingEventsListener, WalletEventsListener
    in/web/
      PortfolioController, dto/
    out/
      persistence/, events/
```

## Domain

`Position` represents a position for `(owner, symbol)`.

- `open(...)`: opens a new position.
- `increase(...)`: increases quantity and recalculates weighted average cost.
- `reduce(...)`: sells partially or fully and calculates realized PnL.
- `investedAmount()`: returns `quantity * averageCost`.
- `isClosed()`: indicates whether the open quantity is zero.

Closed positions are saved with zero quantity to preserve historical realized PnL.

`CashProjection` stores the latest available-cash snapshot received from wallet.

`PortfolioSnapshot` composes the aggregate response: cash, cost basis, realized PnL, open positions, and market valuations.

## Application Service

`PortfolioService` implements three flows:

1. `handle(OrderExecutedEvent)`: updates positions after confirmed execution.
2. `handle(AvailableCashUpdatedEvent)`: saves the available-cash projection.
3. `getByOwner(owner)`: builds the snapshot queried by REST and includes current prices through `MarketPriceLookupPort`.

## REST API

```http
GET /api/portfolio
Authorization: Bearer <jwt>
```

Returns the authenticated user's portfolio.

## Events Consumed

- `OrderExecutedEvent`: comes from wallet/trading when an order was settled successfully.
- `AvailableCashUpdatedEvent`: comes from wallet every time available balance changes.

## Events Published

- `PositionOpenedEvent`
- `PositionIncreasedEvent`
- `PositionReducedEvent`
- `PositionClosedEvent`
- `PortfolioValuationUpdatedEvent`

## BUY Flow

1. Portfolio receives `OrderExecutedEvent` with `side = BUY`.
2. If there is no position, it creates `Position.open(...)`.
3. If one already exists, it uses `Position.increase(...)`.
4. It publishes a position opened or increased event.
5. It publishes `PortfolioValuationUpdatedEvent`.

## SELL Flow

1. Portfolio receives `OrderExecutedEvent` with `side = SELL`.
2. It looks for an existing position.
3. It applies `Position.reduce(...)`.
4. If quantity reaches zero, it saves the closed position and publishes `PositionClosedEvent`.
5. If quantity remains open, it publishes `PositionReducedEvent`.
6. It publishes `PortfolioValuationUpdatedEvent`.
