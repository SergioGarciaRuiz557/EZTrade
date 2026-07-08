# Market Module

## Purpose

`market` provides market data to HTTP clients and to other modules. It queries Alpha Vantage, validates symbols, and caches responses to reduce external calls.

## Domain

- `Symbol`: value object that validates and normalizes the ticker.
- `MarketPrice`: current price for a symbol and timestamp.
- `Instrument`: instrument search result.
- `InstrumentOverview`: summarized fundamental data.
- `Candle`: historical OHLCV candle.
- `InvalidSymbolException` and `ExternalApiException`: module-specific errors.

## Application

Input ports:

- `GetPriceUserCase`
- `SearchInstrumentUserCase`
- `GetOverviewUserCase`
- `GetDailyCandlesUserCase`

Services:

- `GetPriceService`
- `SearchInstrumentService`
- `GetOverviewService`
- `GetDailyCandlesService`

Output ports:

- `GetPriceMarketProviderPort`
- `SearchInstrumentProviderPort`
- `GetOverviewProviderPort`
- `GetDailyCandlesProviderPort`

## Adapters

### REST Input

`MarketController` exposes:

| Method | Route | Use |
|---|---|---|
| `GET` | `/api/v1/market/get-price?symbol=AAPL` | Current price. |
| `GET` | `/api/v1/market/search?input=apple` | Instrument search. |
| `GET` | `/api/v1/market/get-overview?symbol=AAPL` | Fundamental overview. |
| `GET` | `/api/v1/market/get-daily-candles?symbol=AAPL` | Daily candles. |

Output DTOs are `MarketPriceResponse`, `InstrumentResponse`, `InstrumentOverviewResponse`, and `CandleResponse`.

### External Output

`AlphaVantageAPI` implements the provider ports. It builds URLs, applies timeouts, controls rate limits, and transforms external JSON into domain models.

### Cache

`CachedMarketDataProvider` decorates the external provider with `@Cacheable`:

- `marketPrice` cache,
- `instrumentOverview` cache,
- `dailyCandles` cache.

## Internal API

`MarketPriceLookupPort` lives in `market/api` and allows trading and portfolio to obtain current prices without depending on internal adapters. Its implementation is `MarketPriceLookupAdapter`.

## Flow

1. The controller receives a symbol or input.
2. The use case delegates to its output port.
3. The cached adapter tries to resolve from cache.
4. If there is no cache hit, `AlphaVantageAPI` queries the external provider.
5. The result is transformed to domain and then to a REST DTO.
