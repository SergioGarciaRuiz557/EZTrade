# Technical Reverse-Engineering Inventory for the EZTrade Repository

This inventory summarizes evidence observed directly in the repository on May 13, 2026. The Git root is in the parent directory `../`, where `.github/` lives, while the application code is inside `EZTrade/` with the `backend/` and `frontend/` subprojects. The generated documentation is located in `docs/engineering-diagrams/` inside the application project.

## Inspected Scope

- Complete structure of `backend/`, `frontend/`, `.env.example`, and `.github/workflows/maven.yml`.
- Maven, Spring Boot, Spring Modulith, JPA, security, WebSocket/STOMP, cache, logging, and environment property configuration.
- Next.js App Router, React components, API clients, authentication context, local/SWR state, and STOMP connection.
- JPA entities, repositories, application services, REST controllers, domain events, and Spring listeners.
- Backend tests, CI workflow, and available DevOps artifacts.
- Explicit review of `docker-compose.yml`, backend/frontend Dockerfiles, Nginx, Traefik, Kubernetes, Terraform, Helm, and equivalent manifests.

## Detected Technology Stack

### Backend

- Java/Spring Boot in `backend/pom.xml`.
- Spring Boot `4.0.1`.
- Spring Modulith `2.0.1`, with modules declared through `@ApplicationModule`.
- Maven Wrapper (`backend/mvnw`, `backend/mvnw.cmd`).
- `java.version=23`, Maven compilation configured with `maven-compiler-plugin` at `release=21`, and CI with JDK 23.
- Spring MVC/Web (`spring-boot-starter-webmvc`), Spring Security, Spring Data JPA, WebSocket/STOMP, Cache, Actuator.
- JWT with `io.jsonwebtoken` `0.13.0`.
- MySQL Connector `8.2.0`, H2 at runtime, and `spring.jpa.hibernate.ddl-auto=update`.
- Caffeine Cache for market prices/overview/candles.
- Spring Events as the internal mechanism for publishing and consuming domain events.

### Frontend

- Next.js `15.x` with App Router (`frontend/app`).
- React `19.x`, TypeScript `5.x`, and Tailwind CSS `3.4.x`.
- SWR for reactive data reads in dashboard, trading, and wallet.
- Radix UI, lucide-react, Recharts, and custom UI components.
- STOMP client with `@stomp/stompjs` for private notifications.
- API and WebSocket endpoint configuration through `NEXT_PUBLIC_API_URL` and `NEXT_PUBLIC_WS_URL`.

### Database

- Inferred relational database: local MySQL through `spring.datasource.url=jdbc:mysql://localhost:3306/EZTrade`.
- Detected JPA entities:
  - `user`
  - `trade_order`
  - `wallet_account`
  - `wallet_ledger_entry`
  - `portfolio_position`
  - `portfolio_cash_projection`
  - `notification_inbox`
- No Flyway/Liquibase migrations or versioned SQL scripts were found.
- Relationships between tables are mostly expressed through logical keys (`owner`, `symbol`, `referenceId`), not through JPA `@ManyToOne`/`@OneToMany` associations or explicit foreign keys in the code.

## Repository Structure

```text
EZTrade/
  .env.example
  backend/
    pom.xml
    mvnw, mvnw.cmd
    README.md
    demo.http
    src/main/java/com/trading/platform/eztrade/
      EzTradeApplication.java
      user/
      security/
      market/
      trading/
      wallet/
      portfolio/
      notifications/
    src/main/resources/application.properties
    src/test/java/com/trading/platform/eztrade/
  frontend/
    package.json
    next.config.ts
    tailwind.config.ts
    app/
    components/
    features/
    lib/
../.github/
  workflows/maven.yml
```

## Backend Modules and Actual Boundaries

The backend is organized as a modular Spring Modulith application. Dependency rules are declared in `package-info.java`:

- `user`: user management, JPA persistence, and public ports `user :: api`.
- `security`: authentication, authorization, JWT, HTTP and STOMP filters. Depends on `user :: api`.
- `market`: price, search, overview, and candle queries through Alpha Vantage, with Caffeine cache.
- `trading`: BUY/SELL order lifecycle, offer marketplace, price validations, and event publication.
- `wallet`: available/reserved balance, auditable ledger, idempotency by `(owner, referenceId, movementType)`, and reaction to trading events.
- `portfolio`: positions, projected cash, market-price valuation, and reaction to wallet/trading events.
- `notifications`: domain event consumption and fan-out to email, push, WebSocket, and inbox.

The test `backend/src/test/java/com/trading/platform/eztrade/ModulithStructureTest.java` runs `ApplicationModules.of(EzTradeApplication.class).verify()`, confirming that modularity is not only documentation: it is part of the verification suite.

## Main HTTP and WebSocket Entry Points

Detected REST controllers:

- `AuthController`: `POST /auth/login`.
- `UserController`: `POST /api/user/register`, `GET /api/user?email=...`.
- `MarketController`: `GET /api/v1/market/get-price`, `/search`, `/get-overview`, `/get-daily-candles`.
- `TradingController`: `POST /api/v1/trading/orders`, `POST /api/v1/trading/orders/{orderId}/execute`, `POST /api/v1/trading/orders/{orderId}/cancel`, `GET /api/v1/trading/orders`, `GET /api/v1/trading/orders/{orderId}`.
- `TradingMarketplaceController`: `POST /api/v1/trading/market/buy`, `POST /api/v1/trading/offers`, `GET /api/v1/trading/offers`, `POST /api/v1/trading/offers/{offerId}/buy`.
- `WalletController`: `POST /api/v1/wallet/deposit`, `/withdraw`, `/transfer`, `GET /balance`, `GET /transactions`.
- `PortfolioController`: `GET /api/portfolio`.

WebSocket/STOMP:

- `WebSocketConfig` registers endpoint `/ws`.
- Simple broker: `/topic`, `/queue`.
- User prefix: `/user`.
- The frontend subscribes to `/user/queue/notifications`.

## Identified Business Flows

- Registration and JWT login: frontend `authApi`, backend `UserController`, `AuthController`, `AuthService`, `JwtService`.
- Direct market buy: `TradingMarketplaceController.buyFromMarket`, `TradingService.buy`, `MarketPriceLookupPort`, `WalletService`, `PortfolioService`, `NotificationService`.
- SELL offer publication and purchase: `TradingMarketplaceController.placeSellOffer`, `buyOffer`, pessimistic lock `findByIdForUpdate`, `OrderPlacedEvent`, and `OrderExecutionRequestEvent`.
- Wallet management: deposit, withdrawal, transfer, reservation, release, and settlement.
- Portfolio query: persisted positions, projected cash, and market valuation on demand.
- Notifications: after-commit events, fan-out to WebSocket, inbox, email, and logging push.

## Detected DevOps Artifacts

- Real workflow: `../.github/workflows/maven.yml`.
- Triggers: `push` and `pull_request` against `main`.
- Runner: `ubuntu-latest`.
- Steps: `actions/checkout@v4`, `actions/setup-java@v4` with Oracle JDK 23 and Maven cache, `mvn -B clean verify` in `EZTrade/backend`.
- Documented environment variables: `.env.example`.
- Local Docker runtime artifacts are present: `docker-compose.yml`, `backend/Dockerfile`, and `frontend/Dockerfile`.
- Compose starts MySQL 8.4, the Spring Boot backend, and the Next.js frontend for local development.
- No Nginx, reverse proxy, Terraform, Kubernetes, Helm, deployment scripts, or image-publishing jobs were found.

## Configuration and Secrets

- Backend:
  - `DB_USERNAME`, `DB_PASSWORD`, `ALPHA_VANTAGE_API_KEY`, `ALPHA_VANTAGE_MIN_INTERVAL_MS`, `MARKET_CACHE_TTL_SECONDS`, `JWT_SECRET`, `JWT_TOKEN_EXPIRATION_MS`, `JWT_REFRESH_WINDOW_MS`, `APP_CORS_ALLOWED_ORIGINS`.
- Frontend:
  - `NEXT_PUBLIC_API_URL`, `NEXT_PUBLIC_WS_URL`.
- Critical observation: `application.properties` includes development defaults, including an Alpha Vantage API key and a development base64 JWT secret. The documentation marks this as local/development configuration, not robust production secret management.

## Detected Observability

- Tomcat access logs enabled in `application.properties`.
- `CommonsRequestLoggingFilter` configured in `HttpObservabilityConfig`.
- Actuator and Spring Modulith observability dependencies in `pom.xml`.
- No Prometheus, Grafana, distributed tracing, exporters, dashboards, alerts, or external log pipeline configuration was found.

## Testing and Quality

- There is a broad backend test suite under `backend/src/test/java`.
- Layer coverage:
  - Domain: `TradeOrderTest`, `WalletAccountTest`, `PositionTest`, `market` models.
  - Application services: trading, wallet, portfolio, market, user, auth, notifications.
  - Controllers: auth, user, market, wallet, portfolio, trading security.
  - Security: JWT, filters, user access.
  - Architecture: `ModulithStructureTest`.
  - Cache: `MarketCachingIntegrationTest`.
- No frontend tests (`*.test.tsx`, `*.spec.tsx`) or e2e configuration were found.
- `frontend/package.json` declares `lint: next lint`, but the current CI workflow does not run frontend build/lint/tests.

## Diagram Candidates Derived from Evidence

High-evidence candidates:

- System context and frontend-backend-database architecture.
- Logical containers: browser, Next.js, Spring Boot, MySQL, Alpha Vantage.
- Spring Modulith modules and allowed dependencies.
- Hexagonal architecture per backend module.
- REST endpoints, security filters, WebSocket/STOMP, and internal events.
- JPA model and mapping to actual tables.
- Login, direct buy, offer purchase, transfer, and notification sequences.
- Real GitHub Actions CI pipeline.
- Configuration, secrets, and observability flow with limitations.
- Testing flow and frontend/DevOps gaps.

Limited or negative evidence:

- Production infrastructure: no IaC, manifests, reverse proxy, or deployment scripts.
- Release strategy: only the `main` branch is evidenced in the workflow.
- Physical database relationships: no explicit JPA FKs; relationships are logical by fields.

## Inference Risks and Limitations

- Previous backend documentation contains some Mermaid diagrams and explanations, but the new diagrams prioritize the current real code over potentially outdated text.
- The database schema is inferred from JPA entities and `ddl-auto=update`; without migrations, there is no versioned snapshot of the final DDL.
- Local runtime is documented through Docker Compose and environment variables; production runtime is not declared.
- Email/push notification channels exist as logging adapters, not as productive external integrations.
- CI does not cover frontend, Docker image builds, or deployment; any quality or release diagram must show that real scope.
- The use of `owner` as a cross-cutting identifier connects user, wallet, trading, portfolio, and notifications logically, but not through explicit relational relationships.
