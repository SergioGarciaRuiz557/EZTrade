# Documentación del Módulo Market

El módulo `market` es un componente de la aplicación EZTrade encargado de proveer al sistema y a los clientes información sobre las condiciones actuales o históricas del mercado financiero. Emplea los principios de Arquitectura Hexagonal (Puertos y Adaptadores) para separar sus responsabilidades en diferentes capas: Dominio, Aplicación y Adaptadores.

## Flujo de Trabajo (Workflow)

El flujo típico de información dentro de este módulo es el siguiente:
1. Una petición HTTP entra por el adaptador web, `MarketController` o por Websocket.
2. El controlador delega la petición invocando a un "puerto de entrada", que es una interfaz que define un caso de uso (ej. `GetPriceUserCase`).
3. El servicio de aplicación pertinente (ej. `GetPriceService`) que implementa la interfaz, recibe la petición.
4. El servicio orquesta la lógica de recuperar la información para los elementos de dominio involucrados, recurriendo a los puertos de salida (ej. `GetPriceMarketProviderPort`).
5. La respuesta viaja de vuelta desde el servicio hacia el cliente a través del controlador, convertido en respuesta HTTP.

## Estructura de Clases

### 1. Capa de Adaptadores (`adapter`)

Contiene los puntos de entrada (in) o salida (out) del núcleo de la aplicación.
En `adapter/in/web` reside el controlador REST que expone el módulo mediante Endpoints de la API:

**`MarketController`**
Controlador principal que expone los endpoints para interactuar con la información bursátil. Su única preocupación es adaptar formatos HTTP al Dominio y viceversa.
```java
@RestController
@RequestMapping("/api/v1/market")
public class MarketController {
    private final GetPriceUserCase getPriceUserCase;

    // ... Constructor ...

    @GetMapping("/price")
    public ResponseEntity<MarketPrice> getPrice(@RequestParam String symbol) {
        return ResponseEntity.ok(getPriceUserCase.getPrice(new Symbol(symbol)));
    }
}
```

### 2. Capa de Aplicación (`application`)

La capa de aplicación funciona como orquestador, compuesta por los Casos de Uso (puertos de entrada) y sus respectivas implementaciones. 

**Puertos de Entrada (Casos de Uso)**
Las interfaces residen en `application/ports/in`. Cada caso de uso define de forma clara una acción que puede realizar el usuario u otro sistema.
Ejemplos: `GetPriceUserCase`, `GetDailyCandlesUserCase`, `GetOverviewUserCase`, `SearchInstrumentUserCase`.

```java
public interface GetPriceUserCase {
    MarketPrice getPrice(Symbol symbol);
}
```

**Servicios (Implementaciones)**
Los implementaciones en `application/services` delegan en su respectivo puerto de salida o integran alguna pequeña lógica necesaria:

**`GetPriceService`**
```java
@Service
public class GetPriceService implements GetPriceUserCase {
    private final GetPriceMarketProviderPort getPriceMarketProviderPort;

    public GetPriceService(GetPriceMarketProviderPort getPriceMarketProviderPort) {
        this.getPriceMarketProviderPort = getPriceMarketProviderPort;
    }

    @Override
    public MarketPrice getPrice(Symbol symbol) {
        return getPriceMarketProviderPort.getMarketPrice(symbol);
    }
}
```

### 3. Capa de Dominio (`domain`)

La capa donde residen las entidades puras y las lógicas intrínsecas del negocio. No posee dependencias ajenas al propio modelo.

**`MarketPrice`**
Representa el precio en el mercado en un instante para un instrumento.
```java
public class MarketPrice {
    // Almacenara el último precio y la marca de tiempo (timestamp)
}
```

**`Symbol`**
Es un tipo de valor (Value Object) que envuelve el identificador de mercado, encapsulando validaciones asociadas a su formato.
```java
public record Symbol(String value) {
    public Symbol {
        if (value == null || value.isBlank()) {
            throw new InvalidSymbolException("Symbol cannot be empty");
        }
    }
}
```

**Excepciones de Dominio**
Contiene excepciones de negocio como `InvalidSymbolException` o `ExternalApiException` si las conexiones hacia el proveedor externo fallan al nutrir los datos de un activo bursátil concreto.

## Resumen de Responsabilidades

| Componente | Capa | Responsabilidad |
|---|---|---|
| `MarketController` | Adaptador IN | Recibir peticiones HTTP de los clientes sobre mercado. |
| `GetPriceUserCase` | Aplicación (Puerto) | Define el contrato para obtener el precio actual. |
| `GetPriceService` | Aplicación (Servicio)| Ejecuta el caso de obtener el precio, usando adaptadores de salida. |
| `MarketPrice`, `Symbol` | Dominio | Representan el lenguaje ubicuo o entidades propias del contexto financiero. |
