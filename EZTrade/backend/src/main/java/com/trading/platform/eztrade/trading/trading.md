# Modulo Trading

## Proposito

`trading` gestiona el ciclo de vida de ordenes de compra/venta y el marketplace entre usuarios. Es un modulo Spring Modulith con arquitectura hexagonal y dependencia permitida hacia `market :: api` para consultar precios actuales.

## Estructura

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

## Dominio

`TradeOrder` es el agregado raiz. Encapsula:

- propietario,
- simbolo normalizado,
- lado (`BUY` o `SELL`),
- cantidad,
- precio,
- estado (`PENDING`, `EXECUTED`, `CANCELLED`),
- fecha de creacion y fecha de ejecucion.

Reglas principales:

- propietario y simbolo son obligatorios,
- cantidad y precio deben ser positivos,
- solo una orden `PENDING` puede ejecutarse,
- solo una orden `PENDING` puede cancelarse,
- solo el propietario puede cancelar su orden,
- `totalAmount()` calcula `price * quantity`.

## Servicio de aplicacion

`TradingService` implementa todos los casos de uso. Sus responsabilidades actuales son:

- crear ordenes con `place(...)`,
- ejecutar ordenes con `execute(...)`,
- cancelar ordenes con `cancel(...)`,
- consultar ordenes por id o propietario,
- comprar directamente al mercado con `buy(...)`,
- publicar y consultar ofertas SELL,
- comprar ofertas SELL de otros usuarios.

Para el marketplace consulta `MarketPriceLookupPort`:

- una compra al mercado obtiene precio actual de market,
- una oferta SELL no puede publicarse con precio superior al precio actual,
- al comprar una oferta se revalida el precio antes de ejecutar.

## API REST

### Ordenes

Base: `/api/v1/trading/orders`

| Metodo | Ruta | Uso |
|---|---|---|
| `POST` | `/api/v1/trading/orders` | Crea una orden manual BUY o SELL. |
| `POST` | `/api/v1/trading/orders/{orderId}/execute` | Ejecuta una orden pendiente. |
| `POST` | `/api/v1/trading/orders/{orderId}/cancel` | Cancela una orden pendiente del usuario autenticado. |
| `GET` | `/api/v1/trading/orders/{orderId}` | Consulta una orden por id. |
| `GET` | `/api/v1/trading/orders` | Lista ordenes del usuario autenticado. |

### Marketplace

Base: `/api/v1/trading`

| Metodo | Ruta | Uso |
|---|---|---|
| `POST` | `/market/buy` | Compra al mercado usando precio actual. |
| `POST` | `/offers` | Publica una oferta SELL. |
| `GET` | `/offers?symbol=AAPL` | Lista ofertas SELL pendientes visibles para el comprador. |
| `POST` | `/offers/{offerId}/buy` | Compra una oferta SELL de otro usuario. |

## Eventos

- `OrderPlacedEvent`: se publica al crear una orden. Wallet lo usa para reservar fondos si es BUY.
- `OrderExecutionRequestEvent`: se publica al ejecutar una orden. Wallet lo liquida y, si todo va bien, publica `OrderExecutedEvent`.
- `OrderExecutedEvent`: representa ejecucion confirmada. Lo consume portfolio para actualizar posiciones y notifications para avisar.
- `OrderCancelledEvent`: se publica al cancelar. Wallet libera reservas si existian.

## Persistencia

`TradeOrderRepositoryPort` es el contrato usado por la aplicacion. La implementacion JPA se reparte en:

- `TradeOrderRepositoryAdapter`: adapta el puerto a Spring Data.
- `TradeOrderMapper`: transforma dominio <-> JPA.
- `TradeOrderJpaEntity`: tabla `trade_order`.
- `SpringDataTradeOrderRepository`: consultas por owner, ofertas pendientes y bloqueos para compra de oferta.

## Flujo: compra al mercado

1. `TradingMarketplaceController.buyFromMarket` recibe simbolo y cantidad.
2. `TradingService.buy` normaliza simbolo y consulta precio actual.
3. Crea una orden BUY pendiente.
4. Publica `OrderPlacedEvent`; wallet reserva efectivo.
5. Ejecuta la orden y publica `OrderExecutionRequestEvent`.
6. Wallet liquida la compra y publica `OrderExecutedEvent`.
7. Portfolio abre o incrementa posicion.

## Flujo: compra de oferta

1. El vendedor publica `POST /api/v1/trading/offers`.
2. Trading valida precio actual y acciones disponibles segun historico de ordenes ejecutadas.
3. El comprador llama a `POST /api/v1/trading/offers/{offerId}/buy`.
4. Trading bloquea la oferta con `findByIdForUpdate`.
5. Crea una BUY para el comprador y ejecuta BUY + SELL.
6. Wallet liquida ambos lados y portfolio refleja las posiciones.
