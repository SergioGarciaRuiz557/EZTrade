# Inventario tecnico de ingenieria inversa del repositorio EZTrade

Este inventario resume la evidencia observada directamente en el repositorio el 13 de mayo de 2026. La raiz Git esta en el directorio padre `../`, donde vive `.github/`, mientras que el codigo de aplicacion esta dentro de `EZTrade/` con los subproyectos `backend/` y `frontend/`. La documentacion generada se ubica en `docs/engineering-diagrams/` dentro del proyecto de aplicacion.

## Alcance inspeccionado

- Estructura completa de `backend/`, `frontend/`, `.env.example` y `.github/workflows/maven.yml`.
- Configuracion Maven, Spring Boot, Spring Modulith, JPA, seguridad, WebSocket/STOMP, cache, logging y propiedades de entorno.
- App Router de Next.js, componentes React, clientes API, contexto de autenticacion, estado local/SWR y conexion STOMP.
- Entidades JPA, repositorios, servicios de aplicacion, controladores REST, eventos de dominio y listeners Spring.
- Pruebas backend, flujo de trabajo CI y artefactos DevOps disponibles.
- Busqueda explicita de `Dockerfile`, `docker-compose`, Nginx, Traefik, Kubernetes, Terraform, Helm y manifests equivalentes.

## Stack tecnologico detectado

### Backend

- Java/Spring Boot en `backend/pom.xml`.
- Spring Boot `4.0.1`.
- Spring Modulith `2.0.1`, con modulos declarados mediante `@ApplicationModule`.
- Maven Wrapper (`backend/mvnw`, `backend/mvnw.cmd`).
- Propiedad `java.version=23`, compilacion Maven configurada con `maven-compiler-plugin` en `release=21` y CI con JDK 23.
- Spring MVC/Web (`spring-boot-starter-webmvc`), Spring Security, Spring Data JPA, WebSocket/STOMP, Cache, Actuator.
- JWT con `io.jsonwebtoken` `0.13.0`.
- MySQL Connector `8.2.0`, H2 en tiempo de ejecucion y `spring.jpa.hibernate.ddl-auto=update`.
- Caffeine Cache para precios/overview/velas de mercado.
- Eventos Spring como mecanismo interno de publicacion y consumo de eventos de dominio.

### Frontend

- Next.js `15.x` con App Router (`frontend/app`).
- React `19.x`, TypeScript `5.x` y Tailwind CSS `3.4.x`.
- SWR para lectura reactiva de datos en dashboard, trading y wallet.
- Radix UI, lucide-react, Recharts y componentes UI propios.
- Cliente STOMP con `@stomp/stompjs` para notificaciones privadas.
- Configuracion de endpoint API y WebSocket mediante `NEXT_PUBLIC_API_URL` y `NEXT_PUBLIC_WS_URL`.

### Base de datos

- Base de datos relacional inferida: MySQL local por `spring.datasource.url=jdbc:mysql://localhost:3306/EZTrade`.
- Entidades JPA detectadas:
  - `user`
  - `trade_order`
  - `wallet_account`
  - `wallet_ledger_entry`
  - `portfolio_position`
  - `portfolio_cash_projection`
  - `notification_inbox`
- No se han encontrado migraciones Flyway/Liquibase ni scripts SQL versionados.
- Las relaciones entre tablas se expresan mayoritariamente por claves logicas (`owner`, `symbol`, `referenceId`), no por asociaciones JPA `@ManyToOne`/`@OneToMany` ni claves foraneas explicitas en el codigo.

## Estructura del repositorio

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

## Modulos backend y limites reales

El backend esta organizado como aplicacion modular Spring Modulith. Las reglas de dependencia se declaran en `package-info.java`:

- `user`: gestion de usuarios, persistencia JPA y puertos publicos `user :: api`.
- `security`: autenticacion, autorizacion, JWT, filtros HTTP y STOMP. Depende de `user :: api`.
- `market`: consulta de precios, busqueda, overview y velas a traves de Alpha Vantage, con cache Caffeine.
- `trading`: ciclo de vida de ordenes BUY/SELL, marketplace de ofertas, validaciones de precio y publicacion de eventos.
- `wallet`: balance disponible/reservado, ledger auditable, idempotencia por `(owner, referenceId, movementType)` y reaccion a eventos de trading.
- `portfolio`: posiciones, cash proyectado, valoracion con precios de mercado y reaccion a eventos de wallet/trading.
- `notifications`: consumo de eventos de dominio y fan-out a email, push, WebSocket e inbox.

El test `backend/src/test/java/com/trading/platform/eztrade/ModulithStructureTest.java` ejecuta `ApplicationModules.of(EzTradeApplication.class).verify()`, lo que confirma que la modularidad no es solo documental: forma parte de la suite de verificacion.

## Entradas HTTP y WebSocket principales

Controladores REST detectados:

- `AuthController`: `POST /auth/login`.
- `UserController`: `POST /api/user/register`, `GET /api/user?email=...`.
- `MarketController`: `GET /api/v1/market/get-price`, `/search`, `/get-overview`, `/get-daily-candles`.
- `TradingController`: `POST /api/v1/trading/orders`, `POST /api/v1/trading/orders/{orderId}/execute`, `POST /api/v1/trading/orders/{orderId}/cancel`, `GET /api/v1/trading/orders`, `GET /api/v1/trading/orders/{orderId}`.
- `TradingMarketplaceController`: `POST /api/v1/trading/market/buy`, `POST /api/v1/trading/offers`, `GET /api/v1/trading/offers`, `POST /api/v1/trading/offers/{offerId}/buy`.
- `WalletController`: `POST /api/v1/wallet/deposit`, `/withdraw`, `/transfer`, `GET /balance`, `GET /transactions`.
- `PortfolioController`: `GET /api/portfolio`.

WebSocket/STOMP:

- `WebSocketConfig` registra endpoint `/ws`.
- Broker simple: `/topic`, `/queue`.
- Prefijo de usuario: `/user`.
- El frontend se suscribe a `/user/queue/notifications`.

## Flujos de negocio identificados

- Registro y login con JWT: frontend `authApi`, backend `UserController`, `AuthController`, `AuthService`, `JwtService`.
- Compra directa al mercado: `TradingMarketplaceController.buyFromMarket`, `TradingService.buy`, `MarketPriceLookupPort`, `WalletService`, `PortfolioService`, `NotificationService`.
- Publicacion y compra de ofertas SELL: `TradingMarketplaceController.placeSellOffer`, `buyOffer`, bloqueo pesimista `findByIdForUpdate`, eventos `OrderPlacedEvent` y `OrderExecutionRequestEvent`.
- Gestion de wallet: deposito, retirada, transferencia, reserva, liberacion y liquidacion.
- Consulta de portfolio: posiciones persistidas, cash proyectado y valoracion de mercado bajo demanda.
- Notificaciones: eventos despues de commit, fan-out a WebSocket, inbox, email y push de logging.

## Artefactos DevOps detectados

- Flujo de trabajo real: `../.github/workflows/maven.yml`.
- Triggers: `push` y `pull_request` contra `main`.
- Runner: `ubuntu-latest`.
- Pasos: `actions/checkout@v4`, `actions/setup-java@v4` con JDK 23 Oracle y cache Maven, `mvn -B clean verify` en `EZTrade/backend`.
- Variables de entorno documentadas: `.env.example`.
- No se han encontrado Dockerfiles, Compose, Nginx, reverse proxy, Terraform, Kubernetes, Helm, scripts de despliegue ni jobs de publicacion de imagenes.
- La contenedorizacion real de la aplicacion no esta definida en el repositorio. Docker se incorpora solo para renderizar PlantUML en esta entrega documental.

## Configuracion y secretos

- Backend:
  - `DB_USERNAME`, `DB_PASSWORD`, `ALPHA_VANTAGE_API_KEY`, `ALPHA_VANTAGE_MIN_INTERVAL_MS`, `MARKET_CACHE_TTL_SECONDS`, `JWT_SECRET`, `JWT_TOKEN_EXPIRATION_MS`, `JWT_REFRESH_WINDOW_MS`, `APP_CORS_ALLOWED_ORIGINS`.
- Frontend:
  - `NEXT_PUBLIC_API_URL`, `NEXT_PUBLIC_WS_URL`.
- Observacion critica: `application.properties` incluye valores por defecto de desarrollo, incluida una API key de Alpha Vantage y un secreto JWT base64 de desarrollo. La documentacion marca esto como configuracion local/de desarrollo, no como gestion robusta de secretos de produccion.

## Observabilidad detectada

- Tomcat access logs activados en `application.properties`.
- `CommonsRequestLoggingFilter` configurado en `HttpObservabilityConfig`.
- Dependencias de Actuator y Spring Modulith observability en `pom.xml`.
- No se ha encontrado configuracion de Prometheus, Grafana, tracing distribuido, exporters, dashboards, alertas ni pipeline de logs externo.

## Testing y calidad

- Existe una suite backend amplia bajo `backend/src/test/java`.
- Cobertura por capas:
  - Dominio: `TradeOrderTest`, `WalletAccountTest`, `PositionTest`, modelos de `market`.
  - Servicios de aplicacion: trading, wallet, portfolio, market, user, auth, notifications.
  - Controladores: auth, user, market, wallet, portfolio, trading security.
  - Seguridad: JWT, filtros, acceso por usuario.
  - Arquitectura: `ModulithStructureTest`.
  - Cache: `MarketCachingIntegrationTest`.
- No se han encontrado pruebas frontend (`*.test.tsx`, `*.spec.tsx`) ni configuracion e2e.
- `frontend/package.json` declara `lint: next lint`, pero el flujo de trabajo CI actual no ejecuta compilacion/lint/pruebas del frontend.

## Diagramas candidatos derivados de evidencia

Se consideran de alta evidencia:

- Contexto del sistema y arquitectura frontend-backend-base de datos.
- Contenedores logicos: navegador, Next.js, Spring Boot, MySQL, Alpha Vantage.
- Modulos Spring Modulith y dependencias permitidas.
- Arquitectura hexagonal por modulo backend.
- Endpoints REST, filtros de seguridad, WebSocket/STOMP y eventos internos.
- Modelo JPA y correspondencia con tablas reales.
- Secuencias de login, compra directa, compra de oferta, transferencia y notificacion.
- Pipeline CI real de GitHub Actions.
- Flujo de configuracion, secretos y observabilidad con limitaciones.
- Flujo de testing y gaps de frontend/DevOps.

Se consideran de evidencia limitada o negativa:

- Topologia Docker de la aplicacion: no hay Dockerfile ni Compose.
- Infraestructura de produccion: no hay IaC, manifiestos, reverse proxy ni scripts de despliegue.
- Estrategia de publicaciones: solo se evidencia la rama `main` en el flujo de trabajo.
- Relaciones fisicas de base de datos: no hay FKs JPA explicitas; las relaciones son logicas por campos.

## Riesgos y limitaciones de inferencia

- La documentacion previa del backend contiene algunos diagramas Mermaid y explicaciones, pero los diagramas nuevos priorizan el codigo real actual sobre cualquier texto potencialmente desactualizado.
- El esquema de base de datos se infiere desde entidades JPA y `ddl-auto=update`; sin migraciones, no hay fotografia versionada del DDL final.
- La tiempo de ejecucion local se infiere de propiedades y variables de entorno, no de Compose ni manifests.
- Los canales email/push de notificaciones existen como adaptadores de logging, no como integraciones externas productivas.
- La CI no cubre frontend ni empaquetado/despliegue; cualquier diagrama de calidad o publicacion debe mostrar ese alcance real.
- El uso de `owner` como identificador transversal conecta usuario, wallet, trading, portfolio y notificaciones de forma logica, pero no mediante relaciones relacionales explicitas.
