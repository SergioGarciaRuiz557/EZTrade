# Backend Diagrams

The backend family documents the Spring Boot architecture from several levels: packages, hexagonal pattern, request flow, domain classes, controller-service-repository map, and inter-module events. The main evidence is in `backend/src/main/java/com/trading/platform/eztrade`, `backend/pom.xml`, `application.properties`, and `backend/src/test/java`.

## Backend Package Structure

[![Backend package structure](./rendered/backend-package-structure.png)](./rendered/backend-package-structure.svg)

**Purpose.** Show the real package organization and layers per module.

**How to read it.** Each main package (`user`, `security`, `market`, `trading`, `wallet`, `portfolio`, `notifications`) appears with domain, application, and adapters where they exist.

**Value.** Supports the bounded-context separation and helps quickly locate technical responsibilities.

## Backend Hexagonal Components

[![Backend hexagonal components](./rendered/backend-hexagonal-components.png)](./rendered/backend-hexagonal-components.svg)

**Purpose.** Explain the ports-and-adapters pattern applied across modules.

**How to read it.** REST/event inputs invoke input ports; services orchestrate domain and output ports; adapters implement persistence, events, Alpha Vantage, and notification channels.

**Value.** Connects the code with hexagonal architecture in an academic and verifiable way.

## Backend HTTP Request Flow

[![Backend HTTP request flow](./rendered/backend-request-flow.png)](./rendered/backend-request-flow.svg)

**Purpose.** Describe the path of an authenticated HTTP request through the backend.

**How to read it.** The flow crosses CORS, `JwtAuthFilter`, user loading, `UserAccessFilter`, controller, use case, domain, and JPA repository.

**Value.** Makes API operational security visible and shows where authentication, authorization, and business rules are applied.

## Domain Class Diagram

[![Domain class diagram](./rendered/domain-class-diagram.png)](./rendered/domain-class-diagram.svg)

**Purpose.** Summarize aggregates, value objects, enums, and domain records.

**How to read it.** `TradeOrder`, `WalletAccount`, `Position`, and `User` are the central elements. `WalletTransaction` represents the auditable ledger, and `PortfolioSnapshot` represents the aggregate portfolio view.

**Value.** Useful for explaining invariants, order lifecycle, balances, positions, and notifications without mixing JPA details.

**Limitation.** It is not intended to be the physical data model; that detail is in the database diagrams.

## Service, Controller, and Repository Map

[![Service, controller, and repository map](./rendered/service-controller-repository-map.png)](./rendered/service-controller-repository-map.svg)

**Purpose.** Relate endpoints, application services, and persistence/provider adapters.

**How to read it.** Arrows from controllers indicate delegation to services; arrows from services indicate ports/adapters used.

**Value.** This is a maintenance guide: it helps identify which classes are touched when a feature changes.

## Backend Event Flow

[![Backend event flow](./rendered/backend-event-flow.png)](./rendered/backend-event-flow.svg)

**Purpose.** Show event communication between `trading`, `wallet`, `portfolio`, and `notifications`.

**How to read it.** `wallet` listens to some trading events synchronously so it can abort when funds are insufficient. `notifications` uses after-commit and asynchronous listeners.

**Value.** Explains decoupling, transactional consistency, and eventual consistency inside the modular monolith.

## Conclusion

The backend is not a flat CRUD application: it combines Spring Modulith, hexagonal architecture, rich domain, events, and adapters. This structure favors maintainability, module-level tests, and independent evolution of business contexts.
