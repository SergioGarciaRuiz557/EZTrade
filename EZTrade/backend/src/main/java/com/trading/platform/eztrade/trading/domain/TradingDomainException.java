package com.trading.platform.eztrade.trading.domain;

/**
 * Excepcion de dominio para violaciones de reglas de negocio en trading.
 * <p>
 * Se lanza desde el agregado y value objects cuando una operacion no respeta
 * las invariantes del dominio.
 */
public class TradingDomainException extends RuntimeException {

    /**
     * Crea la excepcion con un mensaje descriptivo.
     *
     * @param message detalle de la regla violada
     */
    public TradingDomainException(String message) {
        super(message);
    }
}
