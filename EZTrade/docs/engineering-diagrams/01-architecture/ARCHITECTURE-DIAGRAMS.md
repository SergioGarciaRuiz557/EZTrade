# Architecture Diagrams

This section provides the structural view of EZTrade: system boundary, logical containers, Spring Modulith modules, and the frontend-backend-database relationship. The diagrams were obtained by cross-checking `frontend/package.json`, `frontend/lib/api-client.ts`, `backend/pom.xml`, `backend/src/main/resources/application.properties`, REST controllers, `WebSocketConfig`, `AlphaVantageAPI`, and `../.github/workflows/maven.yml`.

## System Context

[![System context](./rendered/system-context.png)](./rendered/system-context.svg)

**Purpose.** Places EZTrade in relation to its users, the external Alpha Vantage API, local MySQL, and CI automation.

**How to read it.** The central rectangle is the owned system. Arrows show confirmed interactions: REST and STOMP from the browser, JDBC to MySQL, HTTPS to Alpha Vantage, and Maven verify from GitHub Actions.

**Value.** This is the most useful diagram for opening a technical report because it defines boundary, actors, and external dependencies before going into internal details.

**Limitation.** Production infrastructure is not shown because, although local Docker Compose exists, there are no production manifests, IaC, reverse proxy, or deployment scripts in the repository.

## High-Level Container Architecture

[![High-level container architecture](./rendered/high-level-container-architecture.png)](./rendered/high-level-container-architecture.svg)

**Purpose.** Decomposes the system into frontend runtime, backend runtime, database, and external integration.

**How to read it.** Next.js concentrates routes, authentication context, API clients, and STOMP. Spring Boot concentrates security, controllers, modules, events, cache, and STOMP broker. MySQL and Alpha Vantage remain outside the backend as infrastructure dependencies.

**Value.** Provides a clear container view for explaining the local Docker/runtime setup, HTTP contracts, WebSocket, and persistence.

**Limitation.** The topology reflects local development containers and logical responsibilities; it is not a production deployment topology.

## Logical Module Dependencies

[![Logical module dependencies](./rendered/logical-module-dependencies.png)](./rendered/logical-module-dependencies.svg)

**Purpose.** Represents Spring Modulith modules and their allowed dependencies.

**How to read it.** Solid arrows are direct dependencies through public ports/APIs; dashed arrows are domain events published and consumed through Spring Events.

**Value.** Justifies modularity: `trading` does not directly depend on `wallet` or `portfolio`; it coordinates through events. `security` only crosses into `user :: api`.

**Evidence.** Each module's `package-info.java` and `ModulithStructureTest`.

## Frontend-Backend-Database Architecture

[![Frontend-backend-database architecture](./rendered/frontend-backend-database-architecture.png)](./rendered/frontend-backend-database-architecture.svg)

**Purpose.** Connects the UI, API, security, application, domain, persistence, and notification perspectives.

**How to read it.** The main reading path goes from Next.js pages to API clients, then to security filters, controllers, services, domain, and adapters. The notification flow returns through STOMP to the client.

**Value.** It is the bridge between software architecture and operational runtime, especially relevant for explaining maintainability and technical traceability.

**Limitation.** The table structure is inferred from JPA; there is no versioned DDL.

## Conclusion

Together, these diagrams show that EZTrade is a modular full-stack application: Next.js frontend, Spring Boot/Spring Modulith backend, relational persistence, and internal events to decouple financial operations, portfolio, and notifications. The documentation also makes visible that production infrastructure is not declared yet.
