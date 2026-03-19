# Módulo Trading

## 1. Propósito del módulo

El módulo `trading` es el responsable de gestionar el ciclo de vida de las órdenes de compra y venta de activos dentro de la aplicación.

Su responsabilidad principal es:

- registrar nuevas órdenes,
- ejecutar órdenes existentes,
- cancelar órdenes pendientes,
- consultar órdenes por identificador o por propietario,
- publicar eventos de dominio para comunicar a otros módulos lo que ha ocurrido.

Este módulo está diseñado para respetar los límites de Spring Modulith y seguir una **arquitectura hexagonal**, de forma que la lógica de negocio quede protegida de detalles técnicos como Spring MVC, JPA o el mecanismo concreto de publicación de eventos.

---

## 2. Objetivos arquitectónicos

La implementación actual del módulo sigue estas reglas:

### 2.1. Arquitectura hexagonal

El módulo está dividido en tres grandes zonas:

- **Dominio**: contiene las reglas de negocio puras.
- **Aplicación**: orquesta casos de uso y define puertos.
- **Adaptadores**: conectan el módulo con el exterior (REST, base de datos, eventos de Spring, etc.).

### 2.2. Dominio puro

La capa `domain` **no depende de Spring, JPA ni de frameworks externos**.

Esto permite que las reglas de negocio puedan probarse en aislamiento y evolucionar sin quedar acopladas a infraestructura.

### 2.3. Dependencias orientadas hacia el dominio

El sentido de las dependencias es siempre hacia dentro:

- los controladores dependen de casos de uso,
- los casos de uso dependen de puertos,
- los adaptadores implementan esos puertos,
- el dominio no depende de nada externo.

### 2.4. Comunicación entre módulos mediante eventos

Cuando ocurre algo relevante en `trading`, el módulo publica eventos de dominio:

- `OrderPlacedEvent`
- `OrderExecutedEvent`
- `OrderCancelledEvent`

De esta forma, otros módulos pueden reaccionar sin introducir acoplamiento directo con los servicios internos de `trading`.

---

## 3. Declaración modular

El paquete raíz del módulo está marcado con `@ApplicationModule` en `src/main/java/com/trading/platform/eztrade/trading/package-info.java`.

Esto hace que Spring Modulith reconozca `trading` como un módulo independiente dentro del sistema.

Además, el test `ModulithStructureTest` ejecuta:

```java
ApplicationModules.of(EzTradeApplication.class).verify();
```

Con ello se valida que la estructura modular del proyecto respeta los límites definidos.

---

## 4. Estructura interna del módulo

```text
trading/
├─ domain/
│  ├─ TradeOrder
│  ├─ OrderId
│  ├─ OrderSide
│  ├─ OrderStatus
│  ├─ Quantity
│  ├─ Money
│  ├─ TradingDomainException
│  └─ events/
│     ├─ OrderPlacedEvent
│     ├─ OrderExecutedEvent
│     └─ OrderCancelledEvent
│
├─ application/
│  ├─ ports/
│  │  ├─ in/
│  │  │  ├─ PlaceOrderUseCase
│  │  │  ├─ ExecuteOrderUseCase
│  │  │  ├─ CancelOrderUseCase
│  │  │  └─ GetOrdersUseCase
│  │  └─ out/
│  │     ├─ TradeOrderRepositoryPort
│  │     └─ DomainEventPublisherPort
│  └─ services/
│     └─ TradingService
│
├─ adapter/
│  ├─ in/web/
│  │  ├─ TradingController
│  │  ├─ TradingExceptionHandler
│  │  └─ dto/
│  │     ├─ PlaceOrderRequest
│  │     └─ TradeOrderResponse
│  └─ out/
│     ├─ events/
│     │  └─ SpringDomainEventPublisher
│     └─ persistence/
│        ├─ TradeOrderRepositoryAdapter
│        ├─ TradeOrderMapper
│        └─ jpa/
│           ├─ TradeOrderJpaEntity
│           └─ SpringDataTradeOrderRepository
```

---

## 5. Modelo de dominio

La parte más importante del módulo está en el agregado `TradeOrder`.

### 5.1. `TradeOrder`: agregado raíz

La clase `TradeOrder` representa una orden de compra o venta y encapsula sus invariantes.

Sus atributos principales son:

- `id`: identificador de la orden,
- `owner`: usuario propietario de la orden,
- `symbol`: símbolo del activo,
- `side`: dirección de la orden (`BUY` o `SELL`),
- `quantity`: cantidad negociada,
- `price`: precio unitario,
- `status`: estado actual,
- `createdAt`: fecha de creación,
- `executedAt`: fecha de ejecución.

### 5.2. Reglas de negocio del agregado

`TradeOrder` aplica estas reglas:

- el propietario es obligatorio,
- el símbolo es obligatorio,
- el símbolo se normaliza a mayúsculas,
- la cantidad debe ser mayor que cero,
- el precio debe ser mayor que cero,
- solo una orden `PENDING` puede ejecutarse,
- solo una orden `PENDING` puede cancelarse,
- solo el propietario puede cancelar su orden.

### 5.3. Estados de una orden

El enum `OrderStatus` define tres estados:

- `PENDING`: orden creada y pendiente,
- `EXECUTED`: orden ya ejecutada,
- `CANCELLED`: orden cancelada.

### 5.4. Lado de la orden

El enum `OrderSide` indica el sentido económico de la operación:

- `BUY`: compra,
- `SELL`: venta.

### 5.5. Value Objects

El dominio utiliza varios value objects para evitar primitivos sin semántica:

#### `OrderId`

Encapsula el identificador de la orden y exige que sea positivo.

#### `Quantity`

Representa la cantidad negociada y exige que sea estrictamente positiva.

#### `Money`

Representa importes monetarios y exige que el valor sea estrictamente positivo.

Además, `Money` ofrece el método `multiply(Quantity)` para calcular el importe total.

### 5.6. Cálculo del total

La orden expone el método:

```java
order.totalAmount()
```

que devuelve `price * quantity`.

Este valor se usa después en la respuesta REST `TradeOrderResponse` como campo `total`.

### 5.7. Excepciones de dominio

Cuando se viola una regla de negocio, el dominio lanza `TradingDomainException`.

Ejemplos:

- `Owner is required`
- `Symbol is required`
- `Quantity must be greater than zero`
- `Money value must be greater than zero`
- `Only pending orders can be executed`
- `Only pending orders can be cancelled`
- `Only the owner can cancel the order`

---

## 6. Capa de aplicación

La capa de aplicación no contiene la lógica de negocio esencial, sino la **orquestación** de los casos de uso.

Su implementación principal es `TradingService`.

### 6.1. Casos de uso de entrada

Los puertos de entrada del módulo son:

- `PlaceOrderUseCase`
- `ExecuteOrderUseCase`
- `CancelOrderUseCase`
- `GetOrdersUseCase`

Estos contratos permiten que cualquier adaptador de entrada invoque el módulo sin conocer detalles internos.

### 6.2. Puertos de salida

Los puertos de salida son:

- `TradeOrderRepositoryPort`: persistencia y consulta de órdenes,
- `DomainEventPublisherPort`: publicación de eventos de dominio.

Gracias a estos puertos, `TradingService` no conoce ni JPA ni Spring Events directamente.

### 6.3. `TradingService`

`TradingService` implementa todos los casos de uso del módulo:

- creación de órdenes,
- ejecución,
- cancelación,
- consulta por id,
- consulta por propietario.

También está anotado con:

- `@Service`
- `@Transactional`

Esto indica que actúa como servicio de aplicación y que sus operaciones transcurren dentro de transacciones.

---

## 7. Adaptadores de entrada: API REST

El adaptador REST es `TradingController`, cuya ruta base es:

```text
/api/v1/trading/orders
```

### 7.1. Crear orden

**Endpoint**

```http
POST /api/v1/trading/orders
```

**Body**

```json
{
  "symbol": "IBM",
  "side": "BUY",
  "quantity": 2,
  "price": 100
}
```

**Qué hace internamente**

1. Lee el usuario autenticado desde `Authentication`.
2. Construye un `PlaceOrderCommand`.
3. Invoca `placeOrderUseCase.place(...)`.
4. Devuelve la orden creada como `TradeOrderResponse`.

**Respuesta**

- código HTTP `201 Created`

### 7.2. Ejecutar orden

**Endpoint**

```http
POST /api/v1/trading/orders/{orderId}/execute
```

**Qué hace internamente**

1. Convierte el `orderId` en `OrderId`.
2. Invoca `executeOrderUseCase.execute(...)`.
3. Devuelve la orden resultante.

**Respuesta**

- código HTTP `200 OK`

> Nota importante: en la implementación actual, la ejecución se hace por identificador de orden y no recibe el usuario autenticado como parámetro del caso de uso. Por tanto, la regla de propietario sí se aplica en cancelación, pero no en ejecución.

### 7.3. Cancelar orden

**Endpoint**

```http
POST /api/v1/trading/orders/{orderId}/cancel
```

**Qué hace internamente**

1. Lee el usuario autenticado.
2. Convierte el `orderId` a `OrderId`.
3. Invoca `cancelOrderUseCase.cancel(orderId, authentication.getName())`.
4. Devuelve la orden cancelada.

**Respuesta**

- código HTTP `200 OK`

Aquí sí se valida la propiedad de la orden desde el dominio.

### 7.4. Consultar una orden por id

**Endpoint**

```http
GET /api/v1/trading/orders/{orderId}
```

Recupera una orden concreta por su identificador.

### 7.5. Consultar mis órdenes

**Endpoint**

```http
GET /api/v1/trading/orders
```

Recupera todas las órdenes cuyo propietario coincide con el usuario autenticado.

---

## 8. DTOs expuestos por la API

### 8.1. `PlaceOrderRequest`

Representa los datos de entrada necesarios para crear una orden:

- `symbol`
- `side`
- `quantity`
- `price`

### 8.2. `TradeOrderResponse`

Representa la salida REST de una orden e incluye:

- `id`
- `owner`
- `symbol`
- `side`
- `quantity`
- `price`
- `total`
- `status`
- `createdAt`
- `executedAt`

El método estático `TradeOrderResponse.from(order)` convierte el agregado de dominio en una representación serializable para API.

---

## 9. Manejo de errores

La clase `TradingExceptionHandler` captura las `TradingDomainException` lanzadas por el dominio o propagadas por la aplicación y las transforma en una respuesta HTTP 400.

Formato de respuesta:

```json
{
  "error": "Only pending orders can be executed"
}
```

Esto permite devolver errores de negocio claros al cliente sin exponer detalles internos del servidor.

---

## 10. Adaptadores de salida

### 10.1. Persistencia

La persistencia se resuelve en varias piezas:

#### `TradeOrderRepositoryPort`

Define el contrato de persistencia que necesita la aplicación:

- `save(TradeOrder order)`
- `findById(OrderId orderId)`
- `findByOwner(String owner)`

#### `TradeOrderRepositoryAdapter`

Implementa ese puerto usando Spring Data JPA.

Su trabajo es:

1. convertir el dominio a entidad JPA,
2. delegar en el repositorio de infraestructura,
3. convertir el resultado de nuevo a dominio.

#### `TradeOrderMapper`

Es el componente que evita que el dominio quede contaminado con anotaciones JPA.

- `toEntity(order)` convierte `TradeOrder` en `TradeOrderJpaEntity`.
- `toDomain(entity)` rehidrata el agregado desde persistencia.

#### `TradeOrderJpaEntity`

Es la entidad JPA que se persiste en la tabla:

```text
trade_order
```

Contiene únicamente datos de infraestructura, no reglas de negocio.

#### `SpringDataTradeOrderRepository`

Es el repositorio JPA que extiende `JpaRepository<TradeOrderJpaEntity, Long>`.

Además, declara la consulta:

```java
List<TradeOrderJpaEntity> findByOwner(String owner)
```

### 10.2. Publicación de eventos

#### `DomainEventPublisherPort`

Es el puerto abstracto que necesita la capa de aplicación para emitir eventos.

#### `SpringDomainEventPublisher`

Implementa ese puerto utilizando `ApplicationEventPublisher` de Spring.

Así, `TradingService` puede publicar eventos sin depender directamente de la tecnología concreta.

---

## 11. Eventos de dominio emitidos

El módulo publica tres eventos:

### 11.1. `OrderPlacedEvent`

Se publica al registrar una orden nueva.

Incluye:

- `orderId`
- `owner`
- `symbol`
- `side`
- `quantity`
- `price`
- `occurredAt`

### 11.2. `OrderExecutedEvent`

Se publica cuando una orden pasa a estado `EXECUTED`.

Incluye:

- `orderId`
- `owner`
- `symbol`
- `occurredAt`

### 11.3. `OrderCancelledEvent`

Se publica cuando una orden pasa a estado `CANCELLED`.

Incluye:

- `orderId`
- `owner`
- `symbol`
- `occurredAt`

---

## 12. Flujo de trabajo del módulo

Esta es la parte más importante: cómo funciona realmente el módulo de principio a fin.

### 12.1. Flujo 1: creación de una orden

#### Paso a paso

1. Un cliente llama a `POST /api/v1/trading/orders`.
2. `TradingController` recibe el `PlaceOrderRequest`.
3. Obtiene el usuario autenticado mediante `authentication.getName()`.
4. Construye un `PlaceOrderCommand`.
5. Llama a `TradingService.place(command)`.
6. `TradingService` crea el agregado con `TradeOrder.place(...)`.
7. El dominio valida:
   - propietario,
   - símbolo,
   - cantidad,
   - precio.
8. La orden nace en estado `PENDING`.
9. `TradingService` persiste la orden mediante `TradeOrderRepositoryPort.save(...)`.
10. El adaptador de persistencia convierte el agregado en entidad JPA.
11. Spring Data JPA guarda la entidad en la tabla `trade_order`.
12. La entidad guardada se convierte otra vez a dominio.
13. `TradingService` publica `OrderPlacedEvent`.
14. El controlador transforma la orden en `TradeOrderResponse`.
15. La API devuelve `201 Created`.

#### Resultado

La orden queda registrada como `PENDING` y ya puede ser consultada, ejecutada o cancelada.

---

### 12.2. Flujo 2: ejecución de una orden

#### Paso a paso

1. Un cliente llama a `POST /api/v1/trading/orders/{orderId}/execute`.
2. `TradingController` crea un `OrderId` a partir del path variable.
3. Invoca `TradingService.execute(orderId)`.
4. `TradingService` busca la orden en el repositorio.
5. Si no existe, lanza `TradingDomainException` con el mensaje `Order not found: ...`.
6. Si existe, llama a `current.execute()`.
7. El agregado verifica que su estado sea `PENDING`.
8. Si la regla se cumple, crea una nueva instancia con:
   - `status = EXECUTED`
   - `executedAt = LocalDateTime.now()`
9. El servicio persiste la orden ya ejecutada.
10. Publica `OrderExecutedEvent`.
11. Devuelve la orden ejecutada en la respuesta REST.

#### Regla clave

Solo se puede ejecutar una orden pendiente.

---

### 12.3. Flujo 3: cancelación de una orden

#### Paso a paso

1. Un cliente llama a `POST /api/v1/trading/orders/{orderId}/cancel`.
2. `TradingController` obtiene el usuario autenticado.
3. Invoca `TradingService.cancel(orderId, requestedBy)`.
4. El servicio busca la orden en persistencia.
5. Si no existe, lanza `TradingDomainException`.
6. Si existe, invoca `current.cancel(requestedBy)`.
7. El dominio comprueba dos reglas:
   - quien cancela debe ser el propietario,
   - la orden debe estar en estado `PENDING`.
8. Si ambas reglas se cumplen, la orden pasa a `CANCELLED`.
9. El servicio guarda la nueva versión de la orden.
10. Se publica `OrderCancelledEvent`.
11. La API devuelve la orden cancelada.

#### Reglas clave

- solo el propietario puede cancelar,
- solo una orden pendiente puede cancelarse.

---

### 12.4. Flujo 4: consulta por id

#### Paso a paso

1. Un cliente llama a `GET /api/v1/trading/orders/{orderId}`.
2. El controlador transforma el valor en `OrderId`.
3. Invoca `getOrdersUseCase.getById(...)`.
4. `TradingService` busca la orden en el repositorio.
5. Si no existe, lanza `TradingDomainException`.
6. Si existe, la devuelve al controlador.
7. El controlador la serializa como `TradeOrderResponse`.

---

### 12.5. Flujo 5: consulta de mis órdenes

#### Paso a paso

1. Un cliente llama a `GET /api/v1/trading/orders`.
2. El controlador obtiene el usuario autenticado.
3. Invoca `getOrdersUseCase.getByOwner(authentication.getName())`.
4. `TradingService` solicita al repositorio las órdenes de ese propietario.
5. El adaptador devuelve la lista de agregados.
6. El controlador transforma cada orden a `TradeOrderResponse`.
7. La API devuelve la colección resultante.

---

## 13. Resumen visual del flujo interno

```text
Cliente HTTP
   |
   v
TradingController
   |
   v
Caso de uso (puerto de entrada)
   |
   v
TradingService
   |\
   | \__ Reglas del dominio mediante TradeOrder / Money / Quantity / OrderId
   |
   +----> TradeOrderRepositoryPort ----> Adaptador JPA ----> Base de datos
   |
   +----> DomainEventPublisherPort ----> Adaptador Spring ----> Eventos del sistema
```

---

## 14. Comportamientos importantes a tener en cuenta

### 14.1. El dominio es inmutable a nivel de agregado

`TradeOrder` no modifica su propio estado internamente; en lugar de eso, devuelve nuevas instancias al ejecutar o cancelar.

Esto hace que las transiciones sean más explícitas y fáciles de razonar.

### 14.2. La fecha de ejecución solo se rellena al ejecutar

- al crear una orden, `executedAt` es `null`,
- al ejecutar una orden, `executedAt` se establece con `LocalDateTime.now()`.

### 14.3. El símbolo se normaliza

Si se crea una orden con `ibm`, el dominio la convertirá a `IBM`.

### 14.4. La consulta por propietario sí está filtrada

El endpoint `GET /api/v1/trading/orders` devuelve únicamente las órdenes del usuario autenticado.

### 14.5. La consulta por id y la ejecución operan por identificador

En la implementación actual:

- `getById(orderId)` recupera una orden por id,
- `execute(orderId)` ejecuta una orden por id,

sin comprobar en el caso de uso el propietario autenticado.

La validación de propiedad está implementada explícitamente en la cancelación.

---

## 15. Pruebas existentes del módulo

Actualmente hay pruebas unitarias y de estructura modular.

### 15.1. `TradeOrderTest`

Comprueba aspectos fundamentales del dominio:

- que `place(...)` crea una orden `PENDING`,
- que el símbolo se guarda en mayúsculas,
- que `totalAmount()` calcula correctamente el total,
- que `execute()` cambia el estado a `EXECUTED`,
- que `cancel(...)` falla si quien cancela no es el propietario.

### 15.2. `TradingServiceTest`

Comprueba la orquestación de aplicación:

- que `place(...)` guarda la orden,
- que `place(...)` publica `OrderPlacedEvent`,
- que `execute(...)` cambia el estado,
- que `execute(...)` publica `OrderExecutedEvent`.

### 15.3. `ModulithStructureTest`

Comprueba que la estructura modular del sistema es válida según Spring Modulith.

---

## 16. Ejecución de tests relacionados con trading

```powershell
.\mvnw.cmd -Dtest="TradeOrderTest,TradingServiceTest,ModulithStructureTest" test
```

---

## 17. Conclusión

El módulo `trading` está construido como un contexto bien encapsulado que:

- protege la lógica de negocio en el dominio,
- expone casos de uso claros,
- desacopla infraestructura mediante puertos,
- publica eventos para integrarse con otros módulos,
- mantiene una estructura compatible con Spring Modulith.

En conjunto, esto permite que el módulo evolucione con bajo acoplamiento y con reglas de negocio bien localizadas.

