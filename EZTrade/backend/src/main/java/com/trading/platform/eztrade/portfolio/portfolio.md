# Módulo `portfolio`

> **Ubicación**: `src/main/java/com/trading/platform/eztrade/portfolio/`

Este módulo modela y mantiene la **cartera** de un usuario:

* **Posiciones** por símbolo (cantidad, coste medio y PnL realizado).
* **Cash disponible** (mediante una proyección local de sólo lectura sincronizada con el módulo de `wallet`).
* Reacciona a ejecuciones de órdenes emitidas por `trading`.
* Publica eventos cuando cambian posiciones y cuando se recalcula una “valoración” agregada de la cartera.

La descripción oficial del módulo y sus límites están en `portfolio/package-info.java` (líneas **1–20**).

---

## 1) Responsabilidades y límites (qué hace / qué NO hace)

### ✅ Responsabilidades

Según `portfolio/package-info.java` (**4–10**), portfolio:

1. Mantiene posiciones por usuario y símbolo.
2. Contiene una vista del cash disponible por usuario basada en eventos del módulo `wallet`.
3. Reacciona a eventos de ejecución de órdenes (`trading`).
4. Publica eventos de cambios de posición y de valoración agregada.

### ❌ Fuera de alcance

Según `portfolio/package-info.java` (**12–16**):

* No ejecuta órdenes (pertenece a `trading`).
* No consulta precios de mercado directamente (no hay dependencia directa con `market`).
* No decide la mutación real del cash del usuario de manera transaccional. Esto es propiedad exclusiva de `wallet`.

### Dependencias permitidas (Spring Modulith)

El módulo está declarado como `@ApplicationModule` con:

* `allowedDependencies = {"trading :: events", "wallet :: events"}`

en `portfolio/package-info.java` (**18–20**). Es decir: **portfolio sólo puede depender de submódulos de eventos tanto de trading como de wallet**.

---

## 2) Arquitectura: Hexagonal (Ports & Adapters)

Este módulo sigue una separación típica:

* **Dominio** (`portfolio/domain`): reglas de negocio puras.
* **Aplicación** (`portfolio/application`): orquestación y casos de uso.
* **Adaptadores** (`portfolio/adapter`): entradas (eventos de trading y wallet) y salidas (persistencia + publicación de eventos).

### 2.1. Vista de componentes (alto nivel)

```mermaid
flowchart LR

  %% =====================
  %% Estilos (colores + bordes)
  %% =====================
  classDef trading fill:#FFE8A3,stroke:#B38600,stroke-width:2px,color:#111;
  classDef wallet fill:#D4A5A5,stroke:#8B5A5A,stroke-width:2px,color:#111;
  classDef adapter fill:#BFE9FF,stroke:#0077B6,stroke-width:2px,color:#111;
  classDef app fill:#CFF7D3,stroke:#2D6A4F,stroke-width:2px,color:#111;
  classDef domain fill:#FFD6E7,stroke:#C9184A,stroke-width:2px,color:#111;
  classDef out fill:#E9ECEF,stroke:#495057,stroke-width:2px,color:#111;

  %% =====================
  %% Vista tipo “póster”: pocos bloques grandes
  %% =====================
  OE(["📨 OrderExecutedEvent\n(trading)"]):::trading
  WC(["📨 AvailableCashUpdatedEvent\n(wallet)"]):::wallet
  IN(["🎧 TradingEventsListener\n(adapter/in)"]):::adapter
  WL(["🎧 WalletEventsListener\n(adapter/in)"]):::adapter
  WEB(["🌐 PortfolioController\n(adapter/in/web)"]):::adapter
  SVC(["🧠 PortfolioService\n(application)"]):::app
  DOM(["📦 Dominio\nPosition + CashProjection"]):::domain
  DB(["🗄️ Persistencia\n(JPA)"]):::out
  BUS(["📣 Eventos emitidos\n(Position* + ValuationUpdated)"]):::out

  OE ==>|"consume"| IN
  WC ==>|"consume"| WL
  IN ==>|"delegación"| SVC
  WL ==>|"actualiza\nproyección"| SVC
  WEB ==>|"consulta"| SVC
  SVC ==>|"reglas"| DOM
  SVC ==>|"guarda"| DB
  SVC ==>|"publica"| BUS

  %% Flechas más gruesas y con color
  linkStyle 0 stroke:#B38600,stroke-width:4px;
  linkStyle 1 stroke:#8B5A5A,stroke-width:4px;
```

> Si prefieres una vista “arquitectónica” por capas (más detallada), ver **Vista por capas** a continuación.

#### Vista por capas (swimlanes coloreadas)

```mermaid
%%{init: { 'themeVariables': { 'lineColor': '#000000', 'color':'#ffffff' } } }%%
flowchart LR

%% Estilos por capa (Tus colores originales)
  classDef trading fill:#FFE8A3,stroke:#B38600,stroke-width:2px,color:#111;
  classDef wallet fill:#D4A5A5,stroke:#8B5A5A,stroke-width:2px,color:#111;
  classDef adapter fill:#BFE9FF,stroke:#0077B6,stroke-width:2px,color:#111;
  classDef app fill:#CFF7D3,stroke:#2D6A4F,stroke-width:2px,color:#111;
  classDef domain fill:#FFD6E7,stroke:#C9184A,stroke-width:2px,color:#111;
  classDef infra fill:#E9ECEF,stroke:#495057,stroke-width:2px,color:#111;

%% --- External Events ---
  subgraph T["External Events"]
    OE2(["📨 OrderExecutedEvent (Trading)"])
    WCE(["📨 AvailableCashUpdatedEvent (Wallet)"])
  end
  class T trading;
  class OE2 trading;
  class WCE wallet;

%% --- Adapter ---
  subgraph A["Adapter"]
    L(["🎧 TradingEventsListener"])
    WL(["🎧 WalletEventsListener"])
    API(["🌐 PortfolioController (REST)"])
    PUB(["📣 SpringDomainEventPublisher"])
    PRA(["🧩 RepositoryAdapters"])
  end
  class A adapter;
  class L,WL,API,PUB,PRA adapter;

%% --- Application ---
  subgraph B["Application"]
    UC(["📜 UseCases (ports/in)"])
    S(["🧠 PortfolioService"])
    PORTS(["🔌 Ports/out"])
  end
  class B app;
  class UC,S,PORTS app;

%% --- Domain ---
  subgraph C["Domain"]
    POS(["📦 Position"])
    CASH(["🪞 CashProjection"])
    EVT(["🧾 Portfolio events"])
  end
  class C domain;
  class POS,CASH,EVT domain;

%% --- Infra ---
  subgraph I["Infra (JPA)"]
    JPA(["🗄️ SpringData + JpaEntities"])
  end
  class I infra;
  class JPA infra;

%% Flujo
  OE2 -->|event| L
  WCE -->|event| WL
  L -->|use case| UC
  WL -->|use case| UC
  API -->|query| UC
  UC -->|impl| S
  S -->|rules| POS
  S -->|project| CASH
  S -->|emit| EVT
  S -->|ports| PORTS
  PORTS -->|persist| PRA
  PRA -->|JPA| JPA
  PORTS -->|publish| PUB

%% Estilo de línea: Negro puro para que coincida con la punta (marker-end)
  linkStyle default stroke:#000000,stroke-width:2.5px;
```

Referencias:

* Entrada por evento de trading: `adapter/in/events/TradingEventsListener.java`.
* Entrada por evento de wallet: `adapter/in/events/WalletEventsListener.java`.
* Caso de uso real: `application/services/PortfolioService.java`.
* Puertos de salida: `application/ports/out/*.java`.
* Dominio: `domain/Position.java`, `domain/CashProjection.java`.

---

## 3) Flujo principal: “una orden se ejecuta → portfolio se actualiza”

### 3.1. Entrada: `OrderExecutedEvent` (desde `trading`)

`trading` publica el evento `OrderExecutedEvent` (vía Spring Events). Portfolio lo consume con:

* `TradingEventsListener.on(OrderExecutedEvent event)` en `adapter/in/events/TradingEventsListener.java` (**20–23**), que simplemente delega al caso de uso de aplicación.

```mermaid
sequenceDiagram
  participant T as trading (Spring Events)
  participant L as TradingEventsListener
  participant A as PortfolioService
  participant P as PositionRepositoryPort
  participant C as CashAccountRepositoryPort
  participant E as DomainEventPublisherPort

  T->>L: OrderExecutedEvent
  L->>A: handle(event)
  A->>P: findByOwnerAndSymbol(owner, symbol)
  alt BUY
	A->>P: save(open|increase)
	A->>E: publish(PositionOpened|PositionIncreased)
  else SELL
	A->>P: deleteByOwnerAndSymbol OR save(updated)
	A->>E: publish(PositionClosed|PositionReduced)
  end
  A->>A: snapshot = getByOwner(owner) // Lee CashProjection y Positions
  A->>E: publish(PortfolioValuationUpdatedEvent)
```

La lógica completa está en `application/services/PortfolioService.java`:

* Normalización y validaciones base.
* Ruteo por lado BUY/SELL, enfocado puramente en la **creación/actualización de posiciones**.
* Publicación del evento de valoración agregada, leyendo la proyección del cash.

---

## 4) Reglas de negocio (Dominio)

### 4.1. `Position`: cantidad, coste medio y PnL realizado

Archivo: `domain/Position.java`.

Representa una posición **por** `(owner, symbol)`.

**Atributos principales** (líneas **13–18**):

* `quantity`: unidades actuales.
* `averageCost`: coste medio ponderado.
* `realizedPnl`: PnL realizado acumulado.
* `updatedAt`: marca temporal del último cambio.

**Operaciones de negocio**:

* `open(owner, symbol, quantity, executionPrice)` (**34–38**)
  * Abre una posición nueva.
  * Invariante: cantidad y precio deben ser > 0 (**35–36**).

* `increase(quantityToAdd, executionPrice)` (**49–60**)
  * Recalcula el coste medio ponderando coste actual + coste añadido.
  * Fórmula: `newAverageCost = (currentCost + addedCost) / newQuantity` (**53–58**).

* `reduce(quantityToSell, executionPrice)` (**62–87**)
  * Vende parcialmente o cierra.
  * Invariante: no se puede vender más de lo que hay (**66–68**).
  * Cálculo PnL realizado incremental:
	* `realizedDelta = (executionPrice - averageCost) * quantityToSell` (**70–71**).
  * Si se cierra (cantidad 0) el coste medio se pone a 0 (**73–76**).
  * Devuelve `SellResult(position, realizedPnlDelta)` (**86–87**, **121–122**).

**Consultas auxiliares**:

* `investedAmount()` = `quantity * averageCost`.
* `isClosed()` = `quantity == 0`.

### 4.2. `CashProjection`: réplica del cash disponible basada en eventos

Archivo: `domain/CashProjection.java`.

A diferencia de las posiciones que sufren mutaciones de dominio aquí, el módulo `portfolio` **no cambia** activamente el cash. Simplemente mantiene un modelo de *read-only* (proyección local) que se sincroniza escuchando eventos emitidos por el módulo `wallet`.

Al reaccionar ante el evento `AvailableCashUpdatedEvent` de `wallet`, `PortfolioService` extrae la información de dominio y guarda un record de la `CashProjection` actualizando un timestamp.

### 4.3. Excepciones de dominio

* `domain/PortfolioDomainException.java` (**6–11**): excepción runtime para violaciones de invariantes.

---

## 5) Capa de aplicación: casos de uso y orquestación

### 5.1. Puertos de entrada (Use Cases)

Ubicación: `application/ports/in`.

* `HandleOrderExecutedUseCase` (`handle(OrderExecutedEvent event)`) en `HandleOrderExecutedUseCase.java`.
  * Es el contrato que permite que un adaptador de entrada (listener de eventos) invoca la lógica.

* `HandleWalletCashUpdatedUseCase` (`handle(AvailableCashUpdatedEvent event)`) en `HandleWalletCashUpdatedUseCase.java`.
  * Contrato usado para actualizar la proyección del estado de liquidez emitido desde `wallet`.

* `GetPortfolioUseCase` (`getByOwner(String owner)`) en `GetPortfolioUseCase.java`.
  * Contrato de lectura (consulta) de la cartera agregada.

### 5.2. Servicio de aplicación: `PortfolioService`

Archivo: `application/services/PortfolioService.java`.

Implementa ambos casos de uso:

* `implements HandleOrderExecutedUseCase, GetPortfolioUseCase, HandleWalletCashUpdatedUseCase`.

#### Método principal: `handle(OrderExecutedEvent event)`

Se encarga de:

1. Extraer/normalizar datos del evento (`owner`, `symbol`, `quantity`, `price`).
2. Mapear y ejecutar rama BUY o SELL llamando a submétodos privados que actúan exclusivamente sobre repositorios de persistencia *Position*.
3. Recalcular snapshot agregada (accediendo a la `CashProjection`) y publicar `PortfolioValuationUpdatedEvent`.

#### Rama BUY: `handleBuy(...)`

* Si no existe posición → `Position.open(...)` y publica `PositionOpenedEvent`.
* Si existe → `current.increase(...)` y publica `PositionIncreasedEvent`.

#### Rama SELL: `handleSell(...)`

* Requiere posición existente o lanza `PortfolioDomainException`.
* Aplica reducción/cierre con `current.reduce(...)`.
* Si queda cerrada → elimina en repositorio y publica `PositionClosedEvent`.
* Si queda abierta → guarda y publica `PositionReducedEvent`.

#### Método sincronización de balance: `handle(AvailableCashUpdatedEvent event)`

* Recibe el evento emitido por `wallet`, valida parámetros.
* Actualiza o crea el persistente `CashProjection` mediante el repositorio.

#### Lectura agregada: `getByOwner(owner)`

* Carga todas las posiciones (`findByOwner`).
* Suma:
  * `totalCostBasis`: suma de `Position::investedAmount`.
  * `totalRealizedPnl`: suma de `Position::realizedPnl`.
* Lee `cashAvailable` desde la base de datos a través de la interfaz proyectada de `CashProjection` (o devuelve 0).
* Devuelve `PortfolioSnapshot`.

---

## 6) Puertos de salida (dependencias externas)

Ubicación: `application/ports/out`.

### 6.1. Persistencia de posiciones: `PositionRepositoryPort`

Archivo: `application/ports/out/PositionRepositoryPort.java` (**8–17**):

* `findByOwnerAndSymbol(owner, symbol)`
* `findByOwner(owner)`
* `save(position)`
* `deleteByOwnerAndSymbol(owner, symbol)`

### 6.2. Persistencia de la proyección de cash: `CashProjectionRepositoryPort`

Archivo: `application/ports/out/CashProjectionRepositoryPort.java`:

* `findByOwner(owner)`
* `save(projection)`

### 6.3. Publicación de eventos: `DomainEventPublisherPort`

Archivo: `application/ports/out/DomainEventPublisherPort.java` (**3–6**):

* `publish(Object event)`

Este puerto desacopla la capa de aplicación de la tecnología de eventos (Spring).

---

## 7) Adaptadores

### 7.1. Adaptador de entrada: listener de eventos de trading

* `adapter/in/events/TradingEventsListener.java`
  * Método `on(OrderExecutedEvent event)` con `@EventListener`.
  * Traduce el evento de `trading` en una llamada al caso de uso `HandleOrderExecutedUseCase`.

* `adapter/in/events/WalletEventsListener.java`
  * Método `on(AvailableCashUpdatedEvent event)` con `@EventListener`.
  * Se apoya sobre este evento del módulo `wallet` para mantener consistencia de lectura a través del `HandleWalletCashUpdatedUseCase`.

### 7.2. Adaptador de entrada: Web (REST)

* `adapter/in/web/PortfolioController.java`
  * Expone el endpoint `GET /api/portfolio`.
  * Invoca `GetPortfolioUseCase.getByOwner(...)` obteniendo un `PortfolioSnapshot`.
  * Modela la respuesta usando los DTOs `PortfolioResponse` y `PositionResponse`.

### 7.3. Adaptador de salida: publicación de eventos con Spring

* `adapter/out/events/SpringDomainEventPublisher.java`
  * Implementa `DomainEventPublisherPort`.
  * Usa `ApplicationEventPublisher.publishEvent(event)` (**17–18**).

### 7.4. Adaptadores de salida: persistencia (JPA)

#### Posiciones

* `adapter/out/persistence/PositionRepositoryAdapter`
  * Implementa `PositionRepositoryPort`.
* `PositionMapper` / `jpa.PositionJpaEntity`
  * Entidades para usar con Spring Data.

#### Cash (Proyección)

* `adapter/out/persistence/CashProjectionRepositoryAdapter`
  * Implementa `CashProjectionRepositoryPort`.
* `CashProjectionMapper` / `jpa.CashProjectionJpaEntity`
  * Guarda el saldo local actual en base a los eventos del origen que es `wallet`.
