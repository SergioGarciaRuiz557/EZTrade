package com.trading.platform.eztrade.wallet.application.ports.in;

import java.math.BigDecimal;

/**
 * Puerto de entrada para ajustes manuales de fondos del wallet.
 * <p>
 * Se usa para operaciones administrativas o acciones explícitas de usuario (depósito/retiro), así como para cargos de
 * comisiones. Cada operación debe venir identificada con un {@code referenceId} para evitar duplicados en reintentos.
 */
public interface AdjustWalletFundsUseCase {

    /** Ingresa saldo disponible. */
    void deposit(AdjustCommand command);

    /** Retira saldo disponible. */
    void withdraw(AdjustCommand command);

    /** Carga una comisión (actualmente se modela como retiro del disponible). */
    void chargeFee(AdjustCommand command);

    /**
     * Comando de ajuste.
     *
     * @param owner propietario del wallet.
     * @param amount importe (debe ser &gt; 0).
     * @param referenceId identificador idempotente de la operación (p. ej. id externo o uuid).
     * @param description descripción opcional para auditoría.
     */
    record AdjustCommand(String owner, BigDecimal amount, String referenceId, String description) {
    }
}

