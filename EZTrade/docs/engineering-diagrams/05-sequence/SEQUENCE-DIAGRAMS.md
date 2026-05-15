# Diagramas de secuencia

Las secuencias recogen operaciones end-to-end que atraviesan frontend, controladores, servicios, eventos, persistencia y notificaciones. Se han derivado de clientes API en `frontend/features`, controladores REST, servicios de aplicacion, listeners de eventos y adaptadores de salida.

## Secuencia de login JWT

[![Secuencia de login JWT](./rendered/auth-login-sequence.png)](./rendered/auth-login-sequence.svg)

**Proposito.** Mostrar login JWT y restauracion de datos de usuario.

**Como leerlo.** El frontend obtiene un JWT, lo guarda, decodifica el payload solo para UX y carga el usuario completo mediante `/api/user`.

**Valor.** Explica autenticacion, persistencia local de sesion y separacion entre validacion backend y experiencia frontend.

## Secuencia de compra directa al mercado

[![Secuencia de compra directa al mercado](./rendered/buy-from-market-sequence.png)](./rendered/buy-from-market-sequence.svg)

**Proposito.** Modelar la compra directa al mercado.

**Como leerlo.** Trading consulta precio, crea orden BUY, wallet reserva y liquida fondos, portfolio actualiza posicion/cash y notifications informa al usuario.

**Valor.** Es el caso critico mas completo para explicar colaboracion de modulos y eventos.

## Secuencia de compra de oferta en marketplace

[![Secuencia de compra de oferta en marketplace](./rendered/marketplace-offer-purchase-sequence.png)](./rendered/marketplace-offer-purchase-sequence.svg)

**Proposito.** Representar la compra de una oferta SELL entre usuarios.

**Como leerlo.** El servicio bloquea la oferta con `findByIdForUpdate`, valida estado/precio/cobertura, crea BUY del comprador y ejecuta BUY + SELL para propagar efectos en wallet y portfolio.

**Valor.** Documenta concurrencia, consistencia y reglas de marketplace.

## Secuencia de transferencia de wallet

[![Secuencia de transferencia de wallet](./rendered/wallet-transfer-sequence.png)](./rendered/wallet-transfer-sequence.svg)

**Proposito.** Mostrar transferencia de efectivo entre usuarios.

**Como leerlo.** El controlador resuelve destinatario, `WalletService` bloquea cuentas en orden estable, persiste saldos y ledger, y publica eventos de cash disponible.

**Valor.** Explica atomicidad, idempotencia y actualizacion de proyecciones.

## Secuencia de notificacion WebSocket

[![Secuencia de notificacion WebSocket](./rendered/notification-websocket-sequence.png)](./rendered/notification-websocket-sequence.svg)

**Proposito.** Mostrar como un evento de dominio acaba como toast en el frontend.

**Como leerlo.** `DomainEventsListener` procesa despues de commit y en asincrono; `NotificationService` hace fan-out a email/push con logging, WebSocket e inbox.

**Valor.** Conecta consistencia transaccional, mensajeria interna y experiencia near real-time.

## Conclusion

Las secuencias prueban que los flujos importantes no viven en una sola clase: se coordinan mediante puertos, repositorios, eventos y adaptadores. Esto facilita explicar trazabilidad funcional y riesgos de consistencia.
