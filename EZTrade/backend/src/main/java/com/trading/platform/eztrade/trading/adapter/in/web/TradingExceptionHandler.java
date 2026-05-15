package com.trading.platform.eztrade.trading.adapter.in.web;

import com.trading.platform.eztrade.trading.domain.TradingDomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;
import java.util.stream.Collectors;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.joining(", "))
                ))
                .entrySet()
                .stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("; "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", message.isBlank() ? "Invalid request" : message));
    }
}
