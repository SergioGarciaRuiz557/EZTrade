# Modulo Wallet

## Proposito

`wallet` es la fuente de verdad del efectivo de cada usuario. Mantiene saldo disponible, saldo reservado y un ledger auditable de movimientos.

## Responsabilidades

- Crear o reconstruir cuentas de efectivo por usuario.
- Aplicar depositos, retiradas, comisiones y transferencias.
- Reservar fondos para ordenes BUY.
- Liberar fondos al cancelar ordenes.
- Liquidar compras y ventas cuando trading solicita ejecucion.
- Publicar eventos de balance y de fondos.
- Registrar cada movimiento en `WalletTransaction`.

## Dominio

### `WalletAccount`

Entidad inmutable con dos balances:

- `availableBalance`: efectivo libre para retirar, transferir o reservar.
- `reservedBalance`: efectivo retenido para ordenes BUY pendientes de liquidar.

Operaciones principales:

- `deposit(amount)`: aumenta disponible.
- `withdraw(amount)`: reduce disponible si hay saldo suficiente.
- `reserve(amount)`: pasa efectivo de disponible a reservado.
- `release(amount)`: devuelve efectivo reservado a disponible.
- `settleReservedDebit(amount)`: consume reservado en una compra ejecutada.
- `settleCredit(amount)`: abona disponible en una venta ejecutada.
- `chargeFee(amount)`: aplica una comision como retirada.

### `WalletTransaction`

Ledger inmutable. Guarda:

- owner,
- tipo de movimiento,
- importe base,
- delta sobre disponible,
- delta sobre reservado,
- balances finales,
- tipo e id de referencia,
- descripcion,
- fecha de ocurrencia.

### Enums

`MovementType` clasifica movimientos como `DEPOSIT`, `WITHDRAWAL`, `TRANSFER_OUT`, `TRANSFER_IN`, `RESERVE`, `RELEASE`, `SETTLEMENT_DEBIT`, `SETTLEMENT_CREDIT` y `FEE`.

`ReferenceType` indica si el movimiento viene de una orden o de un ajuste manual.

## Servicio de aplicacion

`WalletService` implementa:

- `HandleOrderPlacedUseCase`
- `HandleOrderCancelledUseCase`
- `HandleOrderExecutedUseCase`
- `AdjustWalletFundsUseCase`
- `TransferWalletFundsUseCase`
- `GetWalletBalanceUseCase`
- `GetWalletTransactionsUseCase`

El servicio esta anotado con `@Transactional` para que cuenta y ledger se actualicen de forma atomica.

## API REST

Base: `/api/v1/wallet`

| Metodo | Ruta | Uso |
|---|---|---|
| `POST` | `/deposit` | Ingresa fondos manuales. |
| `POST` | `/withdraw` | Retira fondos disponibles. |
| `POST` | `/transfer` | Transfiere fondos entre usuarios. |
| `GET` | `/balance` | Consulta disponible y reservado. |
| `GET` | `/transactions` | Consulta el ledger del usuario. |

## Eventos consumidos de trading

- `OrderPlacedEvent`: si es BUY, reserva `quantity * price`.
- `OrderCancelledEvent`: libera reserva si existia y no se liquido.
- `OrderExecutionRequestEvent`: liquida BUY o SELL. Despues publica `OrderExecutedEvent`.

## Eventos publicados

- `FundsReservedEvent`: reserva aplicada.
- `FundsReleasedEvent`: reserva liberada.
- `FundsSettledEvent`: liquidacion debit/credit aplicada.
- `InsufficientFundsEvent`: fondos insuficientes.
- `AvailableCashUpdatedEvent`: disponible actualizado para que portfolio sincronice su proyeccion.
- `OrderExecutedEvent`: ejecucion confirmada para portfolio y notifications.

## Idempotencia y concurrencia

Wallet evita duplicar movimientos comprobando `(owner, referenceId, movementType)` antes de aplicar una operacion. Para modificar cuentas usa `findByOwnerForUpdate`, de forma que dos operaciones concurrentes del mismo usuario no calculen balances sobre una foto obsoleta.

## Flujo BUY

1. Trading publica `OrderPlacedEvent`.
2. Wallet reserva el importe de la orden y registra `RESERVE`.
3. Trading publica `OrderExecutionRequestEvent`.
4. Wallet comprueba que existe reserva suficiente.
5. Wallet consume reservado con `SETTLEMENT_DEBIT`.
6. Wallet publica `OrderExecutedEvent` y `AvailableCashUpdatedEvent`.

## Flujo SELL

1. Trading publica `OrderExecutionRequestEvent`.
2. Wallet abona el importe en disponible con `SETTLEMENT_CREDIT`.
3. Wallet publica `OrderExecutedEvent` y `AvailableCashUpdatedEvent`.

## Transferencias

`transfer(...)` carga las dos cuentas en orden determinista para reducir riesgo de interbloqueos, retira al emisor, ingresa al receptor y escribe dos entradas de ledger: `TRANSFER_OUT` y `TRANSFER_IN`.
