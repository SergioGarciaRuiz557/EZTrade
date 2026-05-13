# Roadmap de diagramas de ingenieria

Este roadmap prioriza diagramas con alto valor para una memoria tecnica de TFG/TFC y con trazabilidad clara al repositorio. La primera tanda cubre vision global, backend principal, frontend principal, base de datos, CI/CD, tiempo de ejecucion y secuencias end-to-end. La segunda tanda amplia detalle y operacion.

## Criterios de priorizacion

- Evidencia directa en codigo o configuracion.
- Valor para explicar decisiones arquitectonicas, mantenibilidad y modularidad.
- Capacidad para conectar negocio, plataforma, despliegue y operacion.
- Legibilidad: dividir antes que saturar un unico diagrama.
- Honestidad tecnica: representar incertidumbre o ausencia de artefactos cuando proceda.

## Arquitectura

| Prioridad | Diagrama | Objetivo | Evidencia usada | Detalle | Valor academico |
|---|---|---|---|---|---|
| Alta | `system-context.puml` | Situar EZTrade frente a usuarios, Alpha Vantage y tiempo de ejecucion local | `.env.example`, `application.properties`, clientes API frontend, `AlphaVantageAPI` | Alto nivel | Explica frontera del sistema y actores externos |
| Alta | `high-level-container-architecture.puml` | Mostrar navegador, Next.js, Spring Boot, MySQL y API externa | `frontend/package.json`, `backend/pom.xml`, propiedades datasource, clientes API | Contenedores logicos | Conecta frontend, backend, datos e integraciones |
| Alta | `logical-module-dependencies.puml` | Representar los modulos Spring Modulith y sus dependencias permitidas | `package-info.java`, `ModulithStructureTest` | Modular | Justifica bajo acoplamiento y DDD modular |
| Media | `frontend-backend-database-architecture.puml` | Detallar API REST, WebSocket, JPA y persistencia | controladores, `WebSocketConfig`, entidades JPA | Medio | Explica el flujo tecnico completo de datos |

## Backend

| Prioridad | Diagrama | Objetivo | Evidencia usada | Detalle | Valor academico |
|---|---|---|---|---|---|
| Alta | `backend-package-structure.puml` | Mapear paquetes principales y capas por modulo | `backend/src/main/java/...` | Medio | Visualiza arquitectura hexagonal real |
| Alta | `backend-hexagonal-components.puml` | Mostrar controllers/listeners, use cases, puertos, dominio y adaptadores | controladores, servicios, puertos, repositorios | Medio-alto | Demuestra aplicacion de puertos y adaptadores |
| Alta | `backend-request-flow.puml` | Explicar como una peticion HTTP cruza seguridad, controller, servicio y JPA | `AuthenticationConfig`, `JwtAuthFilter`, controllers, repositories | Medio | Aporta trazabilidad operacional de una peticion |
| Alta | `domain-class-diagram.puml` | Presentar agregados y value objects principales | clases `TradeOrder`, `WalletAccount`, `Position`, `User`, records/eventos | Medio | Sintetiza el modelo de dominio |
| Media | `service-controller-repository-map.puml` | Cruzar controladores, casos de uso, servicios y repositorios | controllers, services, repository adapters | Alto | Facilita mantenimiento y navegacion del codigo |
| Media | `backend-event-flow.puml` | Mostrar eventos intermodulo y listeners | eventos, publishers, listeners | Medio | Explica desacoplamiento y consistencia eventual/sincronica |

## Frontend

| Prioridad | Diagrama | Objetivo | Evidencia usada | Detalle | Valor academico |
|---|---|---|---|---|---|
| Alta | `frontend-structure.puml` | Describir App Router, componentes, features y lib | `frontend/app`, `components`, `features`, `lib` | Medio | Explica organizacion React/Next mantenible |
| Alta | `react-routing-overview.puml` | Representar rutas publicas, auth y dashboard | archivos `app/**/page.tsx`, layouts, `Sidebar` | Medio | Justifica navegacion y proteccion de rutas |
| Alta | `frontend-state-management.puml` | Mostrar AuthContext, localStorage, SWR, estado local y STOMP | `auth-context.tsx`, paginas con SWR, `notifications-ws.tsx` | Medio | Explica gestion de estado real |
| Media | `frontend-backend-interaction.puml` | Relacionar feature APIs con endpoints backend | `features/*/api.ts`, controllers REST | Medio | Conecta UI, contratos HTTP y backend |

## Base de datos

| Prioridad | Diagrama | Objetivo | Evidencia usada | Detalle | Valor academico |
|---|---|---|---|---|---|
| Alta | `database-er-summary.puml` | Resumir tablas, claves y relaciones logicas | entidades JPA, repositorios, campos `owner`, `symbol` | Medio | Da vision relacional sin inventar FKs |
| Alta | `jpa-entity-table-mapping.puml` | Mapear entidad JPA a tabla y agregado/modulo | entidades y mappers JPA | Medio | Explica separacion dominio-persistencia |
| Media | `database-tables-detailed.puml` | Detallar columnas, restricciones e indices principales | anotaciones `@Table`, `@Column`, `@Index`, `@UniqueConstraint` | Alto | Sirve como referencia tecnica de datos |

## Secuencia y comportamiento

| Prioridad | Diagrama | Objetivo | Evidencia usada | Detalle | Valor academico |
|---|---|---|---|---|---|
| Alta | `auth-login-sequence.puml` | Mostrar login JWT y restauracion de sesion | `AuthController`, `AuthService`, `JwtService`, `AuthProvider` | Medio | Explica seguridad de extremo a extremo |
| Alta | `buy-from-market-sequence.puml` | Modelar compra directa con market, wallet, portfolio y notifications | `TradingService.buy`, `WalletService`, `PortfolioService`, listeners | Alto | Caso de uso critico integral |
| Alta | `marketplace-offer-purchase-sequence.puml` | Mostrar compra de oferta SELL entre usuarios | `TradingMarketplaceController`, bloqueo JPA, eventos | Alto | Explica concurrencia y marketplace |
| Media | `wallet-transfer-sequence.puml` | Mostrar transferencia interna entre usuarios | `WalletController.transfer`, `WalletService.transfer`, `UserOwnerLookupPort` | Medio | Explica consistencia transaccional |
| Media | `notification-websocket-sequence.puml` | Mostrar evento de dominio hasta toast frontend | `DomainEventsListener`, `NotificationService`, `WebSocketNotificationAdapter`, `NotificationsWebSocket` | Medio | Conecta backend asincrono con UX |

## DevOps e infraestructura

| Prioridad | Diagrama | Objetivo | Evidencia usada | Detalle | Valor academico |
|---|---|---|---|---|---|
| Alta | `cicd-pipeline-overview.puml` | Representar el flujo de trabajo real de GitHub Actions | `../.github/workflows/maven.yml` | Medio | Documenta automatizacion existente |
| Alta | `build-test-package-flow.puml` | Descomponer compilacion/pruebas backend con Maven | flujo de trabajo, `pom.xml`, pruebas | Medio | Explica verificacion automatizada |
| Alta | `runtime-topology-local.puml` | Mostrar tiempo de ejecucion local inferido: Next, Spring, MySQL, Alpha Vantage | `.env.example`, `application.properties` | Medio | Sustituye una topologia inventada por una real/inferida |
| Alta | `docker-containerization-evidence.puml` | Documentar ausencia de Docker app y uso de Docker solo para PlantUML | busqueda de artefactos Docker, scripts generados | Medio | Evita sobreafirmar infraestructura |
| Media | `configuration-secrets-management.puml` | Representar variables de entorno y configuracion sensible | `.env.example`, `application.properties` | Medio | Aporta lectura de seguridad operativa |
| Media | `observability-logging.puml` | Mostrar logs, Actuator y limites de observabilidad | `HttpObservabilityConfig`, `pom.xml`, properties | Medio | Evalua operabilidad real |

## Calidad y operacion

| Prioridad | Diagrama | Objetivo | Evidencia usada | Detalle | Valor academico |
|---|---|---|---|---|---|
| Alta | `testing-strategy-flow.puml` | Representar tipos de pruebas backend y ejecucion CI | `src/test/java`, flujo de trabajo Maven | Medio | Demuestra estrategia de calidad |
| Alta | `static-quality-and-modulith.puml` | Mostrar verificacion arquitectonica Spring Modulith y lint frontend declarado | `ModulithStructureTest`, `package.json`, flujo de trabajo | Medio | Explica calidad estructural y gaps |
| Media | `release-branching-evidence.puml` | Mostrar estrategia inferible por flujo de trabajo | `maven.yml` | Bajo-medio | Documenta alcance real de publicaciones y ramas |
| Media | `external-dependencies-integrations.puml` | Enumerar integraciones externas y librerias clave | `pom.xml`, `package.json`, `AlphaVantageAPI`, STOMP | Medio | Da trazabilidad de dependencias externas |

## Trazabilidad esperada por documento

Cada Markdown de categoria enlazara los diagramas renderizados como PNG y SVG y citara de forma ligera las rutas usadas como fuente. Los diagramas con evidencia limitada incluiran notas explicitas para separar inferencia razonable de artefacto confirmado.
