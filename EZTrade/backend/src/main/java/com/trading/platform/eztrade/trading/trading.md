# Trading Module

## Purpose

`trading` manages the lifecycle of buy/sell orders and the user-to-user marketplace. It is a Spring Modulith module with hexagonal architecture and an allowed dependency on `market :: api` to query current prices.

## Structure

```text
trading/
  domain/
    TradeOrder, OrderId, OrderSide, OrderStatus, Quantity, Money
    events/
      OrderPlacedEvent
      OrderExecutionRequestEvent
      OrderExecutedEvent
      OrderCancelledEvent
  application/
    ports/in/
      PlaceOrderUseCase, ExecuteOrderUseCase, CancelOrderUseCase, GetOrdersUseCase
      BuyFromMarketUseCase, PlaceSellOfferUseCase, GetSellOffersUseCase, BuySellOfferUseCase
    ports/out/
      TradeOrderRepositoryPort, DomainEventPublisherPort
    services/
      TradingService
  adapter/
    in/web/
      TradingController, TradingMarketplaceController, TradingExceptionHandler, dto/
    out/
      events/SpringDomainEventPublisher
      persistence/TradeOrderRepositoryAdapter, TradeOrderMapper, jpa/
```

## Domain

`TradeOrder` is the aggregate root. It encapsulates:

- owner,
- normalized symbol,
- side (`BUY` or `SELL`),
- quantity,
- price,
- status (`PENDING`, `EXECUTED`, `CANCELLED`),
- creation date and execution date.

Main rules:

- owner and symbol are required,
- quantity and price must be positive,
- only a `PENDING` order can be executed,
- only a `PENDING` order can be cancelled,
- only the owner can cancel their order,
- `totalAmount()` calculates `price * quantity`.

## Application Service

`TradingService` implements all use cases. Its current responsibilities are:

- creating orders with `place(...)`,
- executing orders with `execute(...)`,
- cancelling orders with `cancel(...)`,
- querying orders by id or owner,
- buying directly from the market with `buy(...)`,
- publishing and querying SELL offers,
- buying SELL offers from other users.

For the marketplace, it queries `MarketPriceLookupPort`:

- a market buy obtains the current price from market,
- a SELL offer cannot be published with a price above the current price,
- when an offer is bought, the price is revalidated before execution.

## REST API

### Orders

Base: `/api/v1/trading/orders`

| Method | Route | Use |
|---|---|---|
| `POST` | `/api/v1/trading/orders` | Creates a manual BUY or SELL order. |
| `POST` | `/api/v1/trading/orders/{orderId}/execute` | Executes a pending order. |
| `POST` | `/api/v1/trading/orders/{orderId}/cancel` | Cancels a pending order owned by the authenticated user. |
| `GET` | `/api/v1/trading/orders/{orderId}` | Queries an order by id. |
| `GET` | `/api/v1/trading/orders` | Lists orders for the authenticated user. |

### Marketplace

Base: `/api/v1/trading`

| Method | Route | Use |
|---|---|---|
| `POST` | `/market/buy` | Buys from the market using the current price. |
| `POST` | `/offers` | Publishes a SELL offer. |
| `GET` | `/offers?symbol=AAPL` | Lists pending SELL offers visible to the buyer. |
| `POST` | `/offers/{offerId}/buy` | Buys another user's SELL offer. |

## Events

- `OrderPlacedEvent`: published when an order is created. Wallet uses it to reserve funds if it is a BUY.
- `OrderExecutionRequestEvent`: published when an order is executed. Wallet settles it and, if everything succeeds, publishes `OrderExecutedEvent`.
- `OrderExecutedEvent`: represents confirmed execution. Portfolio consumes it to update positions, and notifications consumes it to notify users.
- `OrderCancelledEvent`: published when an order is cancelled. Wallet releases reservations if they existed.

## Persistence

`TradeOrderRepositoryPort` is the contract used by the application. The JPA implementation is split into:

- `TradeOrderRepositoryAdapter`: adapts the port to Spring Data.
- `TradeOrderMapper`: transforms domain <-> JPA.
- `TradeOrderJpaEntity`: `trade_order` table.
- `SpringDataTradeOrderRepository`: queries by owner, pending offers, and locks for offer purchase.

## Flow: Market Buy

1. `TradingMarketplaceController.buyFromMarket` receives symbol and quantity.
2. `TradingService.buy` normalizes the symbol and queries the current price.
3. It creates a pending BUY order.
4. It publishes `OrderPlacedEvent`; wallet reserves cash.
5. It executes the order and publishes `OrderExecutionRequestEvent`.
6. Wallet settles the buy and publishes `OrderExecutedEvent`.
7. Portfolio opens or increases the position.

## Flow: Offer Purchase

1. The seller publishes `POST /api/v1/trading/offers`.
2. Trading validates current price and available shares according to executed order history.
3. The buyer calls `POST /api/v1/trading/offers/{offerId}/buy`.
4. Trading locks the offer with `findByIdForUpdate`.
5. It creates a BUY for the buyer and executes BUY + SELL.
6. Wallet settles both sides, and portfolio reflects the positions.
