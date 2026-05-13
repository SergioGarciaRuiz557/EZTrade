# Diagramas frontend

Estos diagramas documentan el frontend Next.js/React a partir de `frontend/app`, `frontend/components`, `frontend/features` y `frontend/lib`. El foco esta en estructura, rutas, estado y contratos hacia el backend.

## Estructura frontend

[![Estructura frontend](./rendered/frontend-structure.png)](./rendered/frontend-structure.svg)

**Proposito.** Mostrar como se organiza el proyecto frontend por App Router, componentes reutilizables, features y librerias compartidas.

**Como leerlo.** `app/` define pantallas y layouts; `features/` encapsula clientes API y tipos; `lib/` concentra autenticacion y cliente HTTP; `components/` aporta navegacion, UI y WebSocket.

**Valor.** Explica una estructura mantenible para React sin mezclar vistas, contratos HTTP y utilidades.

## Vista general de rutas React

[![Vista general de rutas React](./rendered/react-routing-overview.png)](./rendered/react-routing-overview.svg)

**Proposito.** Representar rutas publicas, rutas de autenticacion y rutas protegidas.

**Como leerlo.** `(auth)` redirige usuarios ya autenticados; `(dashboard)` bloquea acceso sin token. `Sidebar` articula la navegacion principal y `MarketPage` enlaza con `TradingPage` mediante query params.

**Valor.** Permite justificar experiencia de usuario, proteccion de pantallas y navegacion funcional.

## Gestion de estado frontend

[![Gestion de estado frontend](./rendered/frontend-state-management.png)](./rendered/frontend-state-management.svg)

**Proposito.** Mostrar como se gestiona estado en cliente sin Redux/Zustand.

**Como leerlo.** `AuthProvider` mantiene sesion y `localStorage`; SWR cachea lecturas de portfolio, wallet y ordenes; formularios y dialogos usan `useState`; STOMP alimenta toasts.

**Valor.** Explica decisiones de estado reales y su impacto sobre UX, cache y recuperacion de sesion.

## Interaccion frontend-backend

[![Interaccion frontend-backend](./rendered/frontend-backend-interaction.png)](./rendered/frontend-backend-interaction.svg)

**Proposito.** Mapear clientes API frontend con controladores backend.

**Como leerlo.** Cada `features/*/api.ts` apunta a endpoints concretos: auth/user, market, trading, wallet y portfolio. `fetchWithAuth` centraliza cabecera JWT y manejo de 401.

**Valor.** Da trazabilidad entre UI, contratos HTTP y adaptadores REST backend, util para evolucionar endpoints sin perder impacto.

## Conclusion

El frontend usa una arquitectura pragmaticamente modular: rutas por App Router, features por dominio, estado global minimo y clientes API explicitos. La comunicacion WebSocket se limita a notificaciones privadas, lo que encaja con el backend STOMP.
