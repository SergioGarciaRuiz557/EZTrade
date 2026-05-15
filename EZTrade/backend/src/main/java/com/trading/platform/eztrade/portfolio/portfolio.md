# Modulo Portfolio

## Proposito

`portfolio` mantiene la cartera de cada usuario:

- posiciones abiertas por simbolo,
- coste medio ponderado,
- PnL realizado,
- cash disponible proyectado desde wallet,
- valoraciones actuales de mercado para consultas REST.

El modulo esta declarado con dependencias permitidas hacia `trading :: events`, `wallet :: events` y `market :: api`.

## Limites

Portfolio no ejecuta ordenes y no mueve dinero real. Trading decide el ciclo de vida de ordenes y wallet es la fuente de verdad del efectivo. Portfolio consume eventos para mantener una vista de lectura coherente.

## Estructura

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

## Dominio

`Position` representa una posicion por `(owner, symbol)`.

- `open(...)`: abre una posicion nueva.
- `increase(...)`: aumenta cantidad y recalcula coste medio ponderado.
- `reduce(...)`: vende parcial o totalmente y calcula PnL realizado.
- `investedAmount()`: devuelve `quantity * averageCost`.
- `isClosed()`: indica si la cantidad abierta es cero.

Las posiciones cerradas se guardan con cantidad cero para conservar el PnL realizado historico.

`CashProjection` guarda la ultima foto de cash disponible recibida desde wallet.

`PortfolioSnapshot` compone la respuesta agregada: cash, coste base, PnL realizado, posiciones abiertas y valoraciones de mercado.

## Servicio de aplicacion

`PortfolioService` implementa tres flujos:

1. `handle(OrderExecutedEvent)`: actualiza posiciones tras una ejecucion confirmada.
2. `handle(AvailableCashUpdatedEvent)`: guarda la proyeccion de cash disponible.
3. `getByOwner(owner)`: construye el snapshot consultado por REST e incluye precios actuales mediante `MarketPriceLookupPort`.

## API REST

```http
GET /api/portfolio
Authorization: Bearer <jwt>
```

Devuelve el portfolio del usuario autenticado.

## Eventos consumidos

- `OrderExecutedEvent`: procede de wallet/trading cuando una orden se liquido correctamente.
- `AvailableCashUpdatedEvent`: procede de wallet cada vez que cambia el disponible.

## Eventos publicados

- `PositionOpenedEvent`
- `PositionIncreasedEvent`
- `PositionReducedEvent`
- `PositionClosedEvent`
- `PortfolioValuationUpdatedEvent`

## Flujo BUY

1. Portfolio recibe `OrderExecutedEvent` con `side = BUY`.
2. Si no hay posicion, crea `Position.open(...)`.
3. Si ya existe, usa `Position.increase(...)`.
4. Publica evento de posicion abierta o incrementada.
5. Publica `PortfolioValuationUpdatedEvent`.

## Flujo SELL

1. Portfolio recibe `OrderExecutedEvent` con `side = SELL`.
2. Busca posicion existente.
3. Aplica `Position.reduce(...)`.
4. Si queda cantidad cero, guarda posicion cerrada y publica `PositionClosedEvent`.
5. Si queda cantidad abierta, publica `PositionReducedEvent`.
6. Publica `PortfolioValuationUpdatedEvent`.
