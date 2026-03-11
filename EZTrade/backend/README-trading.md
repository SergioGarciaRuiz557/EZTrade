# Modulo Trading

Este modulo implementa gestion de ordenes (compra/venta) y ejecucion de trades con arquitectura hexagonal.

## Estructura

- `trading/domain`: modelo de dominio puro (sin Spring/JPA).
- `trading/application`: casos de uso y puertos.
- `trading/adapter/in`: API REST.
- `trading/adapter/out`: persistencia JPA y publicacion de eventos.

## Endpoints

- `POST /api/v1/trading/orders`
- `POST /api/v1/trading/orders/{orderId}/execute`
- `POST /api/v1/trading/orders/{orderId}/cancel`
- `GET /api/v1/trading/orders/{orderId}`
- `GET /api/v1/trading/orders`

## Eventos de dominio emitidos

- `OrderPlacedEvent`
- `OrderExecutedEvent`
- `OrderCancelledEvent`

## Ejecucion de tests del modulo

```powershell
.\mvnw.cmd -Dtest="TradeOrderTest,TradingServiceTest,ModulithStructureTest" test
```

