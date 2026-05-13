# Modulo Market

## Proposito

`market` proporciona datos de mercado a clientes HTTP y a otros modulos. Consulta AlphaVantage, valida simbolos y cachea respuestas para reducir llamadas externas.

## Dominio

- `Symbol`: value object que valida y normaliza el ticker.
- `MarketPrice`: precio actual de un simbolo y timestamp.
- `Instrument`: resultado de busqueda de instrumentos.
- `InstrumentOverview`: datos fundamentales resumidos.
- `Candle`: vela historica OHLCV.
- `InvalidSymbolException` y `ExternalApiException`: errores propios del modulo.

## Aplicacion

Puertos de entrada:

- `GetPriceUserCase`
- `SearchInstrumentUserCase`
- `GetOverviewUserCase`
- `GetDailyCandlesUserCase`

Servicios:

- `GetPriceService`
- `SearchInstrumentService`
- `GetOverviewService`
- `GetDailyCandlesService`

Puertos de salida:

- `GetPriceMarketProviderPort`
- `SearchInstrumentProviderPort`
- `GetOverviewProviderPort`
- `GetDailyCandlesProviderPort`

## Adaptadores

### Entrada REST

`MarketController` expone:

| Metodo | Ruta | Uso |
|---|---|---|
| `GET` | `/api/v1/market/get-price?symbol=AAPL` | Precio actual. |
| `GET` | `/api/v1/market/search?input=apple` | Busqueda de instrumentos. |
| `GET` | `/api/v1/market/get-overview?symbol=AAPL` | Resumen fundamental. |
| `GET` | `/api/v1/market/get-daily-candles?symbol=AAPL` | Velas diarias. |

Los DTOs de salida son `MarketPriceResponse`, `InstrumentResponse`, `InstrumentOverviewResponse` y `CandleResponse`.

### Salida externa

`AlphaVantageAPI` implementa los puertos de proveedor. Construye URLs, aplica timeouts, controla rate limit y transforma JSON externo a modelos de dominio.

### Cache

`CachedMarketDataProvider` decora al proveedor externo con `@Cacheable`:

- cache `marketPrice`,
- cache `instrumentOverview`,
- cache `dailyCandles`.

## API interna

`MarketPriceLookupPort` esta en `market/api` y permite a trading y portfolio obtener precios actuales sin depender de adaptadores internos. Su implementacion es `MarketPriceLookupAdapter`.

## Flujo

1. El controlador recibe un simbolo o input.
2. El caso de uso delega en su puerto de salida.
3. El adaptador cacheado intenta resolver desde cache.
4. Si no hay cache, `AlphaVantageAPI` consulta el proveedor externo.
5. El resultado se transforma a dominio y despues a DTO REST.
