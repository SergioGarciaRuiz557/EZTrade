# EZTrade - Demo funcional

Este recorrido valida el flujo completo de un usuario: registro, login, ingreso de fondos, compra al mercado, consulta de portfolio y notificaciones generadas por eventos.

## 1. Registro

```http
POST /api/user/register
Content-Type: application/json

{
  "firstname": "Demo",
  "lastname": "User",
  "username": "demoUser",
  "email": "demo@example.com",
  "password": "password123"
}
```

El backend codifica la password y asigna `Role.USER`.

## 2. Login

El login acepta email o username.

```http
POST /auth/login
Content-Type: application/json

{
  "email": "demo@example.com",
  "password": "password123"
}
```

Respuesta esperada:

```json
{
  "token": "<jwt>",
  "type": "Bearer"
}
```

Desde este punto todas las peticiones protegidas usan:

```http
Authorization: Bearer <jwt>
```

## 3. Ingresar fondos

```http
POST /api/v1/wallet/deposit
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "amount": 1000.00,
  "referenceId": "demo-deposit-001",
  "description": "Ingreso inicial para demo"
}
```

Consulta de balance:

```http
GET /api/v1/wallet/balance
Authorization: Bearer <jwt>
```

## 4. Consultar mercado

```http
GET /api/v1/market/get-price?symbol=AAPL
Authorization: Bearer <jwt>
```

Otros endpoints utiles:

```http
GET /api/v1/market/search?input=apple
GET /api/v1/market/get-overview?symbol=AAPL
GET /api/v1/market/get-daily-candles?symbol=AAPL
```

## 5. Compra directa al mercado

Este es el flujo recomendado para una compra real dentro del backend actual. El cliente no envia precio: `trading` consulta `market :: api`, crea una orden BUY y la ejecuta.

```http
POST /api/v1/trading/market/buy
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "symbol": "AAPL",
  "quantity": 2
}
```

Flujo interno:

1. `TradingMarketplaceController` recibe la peticion.
2. `TradingService` consulta precio actual mediante `MarketPriceLookupPort`.
3. Se crea una orden BUY pendiente.
4. `OrderPlacedEvent` hace que wallet reserve fondos.
5. `OrderExecutionRequestEvent` hace que wallet liquide la compra.
6. Wallet publica `OrderExecutedEvent`.
7. Portfolio abre o incrementa la posicion.
8. Notifications genera mensajes por sus canales.

## 6. Consulta de ordenes y portfolio

```http
GET /api/v1/trading/orders
Authorization: Bearer <jwt>
```

```http
GET /api/portfolio
Authorization: Bearer <jwt>
```

La respuesta de portfolio incluye cash proyectado, posiciones abiertas, coste base, PnL realizado y valoraciones de mercado por simbolo cuando hay precios disponibles.

## 7. Marketplace entre usuarios

Para publicar una oferta de venta:

```http
POST /api/v1/trading/offers
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "symbol": "AAPL",
  "quantity": 1,
  "price": 150.00
}
```

Reglas principales:

- el vendedor debe tener acciones suficientes,
- el precio no puede superar el precio actual de mercado,
- una oferta pendiente no puede ser comprada por su propio vendedor.

Para listar ofertas disponibles:

```http
GET /api/v1/trading/offers?symbol=AAPL
Authorization: Bearer <jwt>
```

Para comprar una oferta de otro usuario:

```http
POST /api/v1/trading/offers/{offerId}/buy
Authorization: Bearer <jwt>
```

## 8. Notificaciones

Notifications escucha eventos de trading, wallet y portfolio. Los canales actuales son:

- logs de email,
- logs de push,
- WebSocket STOMP en `/user/queue/notifications`,
- inbox persistido en base de datos.

Eventos habituales en esta demo:

- `ORDER_PLACED`
- `ORDER_EXECUTED`
- `PORTFOLIO_VALUATION_UPDATED`
- `INSUFFICIENT_FUNDS`, si wallet no puede reservar o liquidar.
