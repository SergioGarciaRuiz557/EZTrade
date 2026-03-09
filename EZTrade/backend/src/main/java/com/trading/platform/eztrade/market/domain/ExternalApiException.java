package com.trading.platform.eztrade.market.domain;

/**
 * Excepción de dominio que representa errores al comunicarse con APIs externas
 * relacionadas con el mercado (por ejemplo, proveedores de datos como Alpha Vantage).
 * <p>
 * Se utiliza para encapsular tanto errores de red como respuestas inválidas o
 * inesperadas por parte del proveedor externo.
 */
public class ExternalApiException extends RuntimeException {
    public ExternalApiException(String message) {
        super(message);
    }

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
