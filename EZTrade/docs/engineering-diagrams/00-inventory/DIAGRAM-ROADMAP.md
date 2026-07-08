# Engineering Diagram Roadmap

This roadmap prioritizes diagrams with high value for a technical final-degree-project report and clear traceability to the repository. The first batch covers global view, main backend, main frontend, database, CI/CD, runtime, and end-to-end sequences. The second batch adds detail and operations.

## Prioritization Criteria

- Direct evidence in code or configuration.
- Value for explaining architectural decisions, maintainability, and modularity.
- Ability to connect business, platform, deployment, and operations.
- Readability: split diagrams before overloading a single one.
- Technical honesty: represent uncertainty or absence of artifacts when appropriate.

## Architecture

| Priority | Diagram | Objective | Evidence used | Detail | Academic value |
|---|---|---|---|---|---|
| High | `system-context.puml` | Place EZTrade in relation to users, Alpha Vantage, and local runtime | `.env.example`, `application.properties`, frontend API clients, `AlphaVantageAPI` | High level | Explains system boundary and external actors |
| High | `high-level-container-architecture.puml` | Show browser, Next.js, Spring Boot, MySQL, and external API | `frontend/package.json`, `backend/pom.xml`, datasource properties, API clients | Logical containers | Connects frontend, backend, data, and integrations |
| High | `logical-module-dependencies.puml` | Represent Spring Modulith modules and their allowed dependencies | `package-info.java`, `ModulithStructureTest` | Modular | Justifies low coupling and modular DDD |
| Medium | `frontend-backend-database-architecture.puml` | Detail REST API, WebSocket, JPA, and persistence | controllers, `WebSocketConfig`, JPA entities | Medium | Explains the full technical data flow |

## Backend

| Priority | Diagram | Objective | Evidence used | Detail | Academic value |
|---|---|---|---|---|---|
| High | `backend-package-structure.puml` | Map main packages and layers per module | `backend/src/main/java/...` | Medium | Visualizes the real hexagonal architecture |
| High | `backend-hexagonal-components.puml` | Show controllers/listeners, use cases, ports, domain, and adapters | controllers, services, ports, repositories | Medium-high | Demonstrates ports and adapters |
| High | `backend-request-flow.puml` | Explain how an HTTP request crosses security, controller, service, and JPA | `AuthenticationConfig`, `JwtAuthFilter`, controllers, repositories | Medium | Provides operational traceability for a request |
| High | `domain-class-diagram.puml` | Present main aggregates and value objects | `TradeOrder`, `WalletAccount`, `Position`, `User`, records/events | Medium | Summarizes the domain model |
| Medium | `service-controller-repository-map.puml` | Cross-reference controllers, use cases, services, and repositories | controllers, services, repository adapters | High | Helps maintenance and code navigation |
| Medium | `backend-event-flow.puml` | Show inter-module events and listeners | events, publishers, listeners | Medium | Explains decoupling and eventual/synchronous consistency |

## Frontend

| Priority | Diagram | Objective | Evidence used | Detail | Academic value |
|---|---|---|---|---|---|
| High | `frontend-structure.puml` | Describe App Router, components, features, and lib | `frontend/app`, `components`, `features`, `lib` | Medium | Explains maintainable React/Next organization |
| High | `react-routing-overview.puml` | Represent public, auth, and dashboard routes | `app/**/page.tsx`, layouts, `Sidebar` | Medium | Justifies navigation and route protection |
| High | `frontend-state-management.puml` | Show AuthContext, localStorage, SWR, local state, and STOMP | `auth-context.tsx`, pages with SWR, `notifications-ws.tsx` | Medium | Explains actual state management |
| Medium | `frontend-backend-interaction.puml` | Relate feature APIs to backend endpoints | `features/*/api.ts`, REST controllers | Medium | Connects UI, HTTP contracts, and backend |

## Database

| Priority | Diagram | Objective | Evidence used | Detail | Academic value |
|---|---|---|---|---|---|
| High | `database-er-summary.puml` | Summarize tables, keys, and logical relationships | JPA entities, repositories, `owner`, `symbol` fields | Medium | Gives a relational view without inventing FKs |
| High | `jpa-entity-table-mapping.puml` | Map JPA entity to table and aggregate/module | JPA entities and mappers | Medium | Explains domain-persistence separation |
| Medium | `database-tables-detailed.puml` | Detail main columns, constraints, and indexes | `@Table`, `@Column`, `@Index`, `@UniqueConstraint` annotations | High | Serves as technical data reference |

## Sequence and Behavior

| Priority | Diagram | Objective | Evidence used | Detail | Academic value |
|---|---|---|---|---|---|
| High | `auth-login-sequence.puml` | Show JWT login and session restoration | `AuthController`, `AuthService`, `JwtService`, `AuthProvider` | Medium | Explains end-to-end security |
| High | `buy-from-market-sequence.puml` | Model direct buy with market, wallet, portfolio, and notifications | `TradingService.buy`, `WalletService`, `PortfolioService`, listeners | High | Critical complete use case |
| High | `marketplace-offer-purchase-sequence.puml` | Show SELL offer purchase between users | `TradingMarketplaceController`, JPA locking, events | High | Explains concurrency and marketplace |
| Medium | `wallet-transfer-sequence.puml` | Show internal transfer between users | `WalletController.transfer`, `WalletService.transfer`, `UserOwnerLookupPort` | Medium | Explains transactional consistency |
| Medium | `notification-websocket-sequence.puml` | Show domain event to frontend toast | `DomainEventsListener`, `NotificationService`, `WebSocketNotificationAdapter`, `NotificationsWebSocket` | Medium | Connects asynchronous backend with UX |

## DevOps and Infrastructure

| Priority | Diagram | Objective | Evidence used | Detail | Academic value |
|---|---|---|---|---|---|
| High | `cicd-pipeline-overview.puml` | Represent the real GitHub Actions workflow | `../.github/workflows/maven.yml` | Medium | Documents existing automation |
| High | `build-test-package-flow.puml` | Break down backend compilation/tests with Maven | workflow, `pom.xml`, tests | Medium | Explains automated verification |
| High | `runtime-topology-local.puml` | Show Docker Compose local runtime: Next, Spring, MySQL, Alpha Vantage | `docker-compose.yml`, Dockerfiles, `.env.example`, `application.properties` | Medium | Documents the actual local runtime topology |
| High | `docker-containerization-evidence.puml` | Document local app containerization and distinguish it from production deployment | Docker Compose, Dockerfiles, generated diagram scripts | Medium | Avoids overstating production infrastructure |
| Medium | `configuration-secrets-management.puml` | Represent environment variables and sensitive configuration | `.env.example`, `application.properties` | Medium | Adds an operational security reading |
| Medium | `observability-logging.puml` | Show logs, Actuator, and observability limits | `HttpObservabilityConfig`, `pom.xml`, properties | Medium | Evaluates real operability |

## Quality and Operations

| Priority | Diagram | Objective | Evidence used | Detail | Academic value |
|---|---|---|---|---|---|
| High | `testing-strategy-flow.puml` | Represent backend test types and CI execution | `src/test/java`, Maven workflow | Medium | Demonstrates quality strategy |
| High | `static-quality-and-modulith.puml` | Show Spring Modulith architectural verification and declared frontend lint | `ModulithStructureTest`, `package.json`, workflow | Medium | Explains structural quality and gaps |
| Medium | `release-branching-evidence.puml` | Show strategy inferable from the workflow | `maven.yml` | Low-medium | Documents real scope of releases and branches |
| Medium | `external-dependencies-integrations.puml` | List external integrations and key libraries | `pom.xml`, `package.json`, `AlphaVantageAPI`, STOMP | Medium | Gives external dependency traceability |

## Expected Traceability per Document

Each category Markdown will link the rendered diagrams as PNG and SVG and lightly cite the paths used as sources. Diagrams with limited evidence will include explicit notes separating reasonable inference from confirmed artifact.
