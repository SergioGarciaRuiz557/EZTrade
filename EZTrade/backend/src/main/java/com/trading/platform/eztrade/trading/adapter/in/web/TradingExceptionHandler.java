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
 * Handles trading module domain errors in the web layer.
 * <p>
 * Translates business exceptions into HTTP responses that clients can understand.
 */
@RestControllerAdvice(basePackages = "com.trading.platform.eztrade.trading.adapter.in.web")
public class TradingExceptionHandler {

    /**
     * Converts a {@link TradingDomainException} into an HTTP 400 response.
     *
     * @param ex captured domain exception
     * @return JSON body with the error message
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
