package com.trading.platform.eztrade.trading.adapter.in.web;

import com.trading.platform.eztrade.trading.domain.TradingDomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Maneja errores de dominio del modulo trading en la capa web.
 * <p>
 * Traduce excepciones de negocio a respuestas HTTP comprensibles para el cliente.
 */
@RestControllerAdvice(basePackages = "com.trading.platform.eztrade.trading.adapter.in.web")
public class TradingExceptionHandler {

    /**
     * Convierte una {@link TradingDomainException} en respuesta HTTP 400.
     *
     * @param ex excepcion de dominio capturada
     * @return cuerpo JSON con el mensaje de error
     */
    @ExceptionHandler(TradingDomainException.class)
    public ResponseEntity<Map<String, String>> handleDomain(TradingDomainException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }
}
