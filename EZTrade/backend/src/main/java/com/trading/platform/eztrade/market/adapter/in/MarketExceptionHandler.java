package com.trading.platform.eztrade.market.adapter.in;

import com.trading.platform.eztrade.market.domain.ExternalApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Traduce errores del modulo market a respuestas HTTP claras para el cliente.
 */
@RestControllerAdvice(basePackages = "com.trading.platform.eztrade.market.adapter.in")
public class MarketExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        if ("symbol".equals(ex.getName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid symbol format: " + ex.getValue()));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid request parameter: " + ex.getName()));
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<Map<String, String>> handleExternalApi(ExternalApiException ex) {
        if (isRateLimitMessage(ex.getMessage())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header(HttpHeaders.RETRY_AFTER, "60")
                    .body(Map.of(
                            "error", ex.getMessage(),
                            "recommendation", "Rate limit alcanzado. Reintenta en 60 segundos y reduce la frecuencia de consultas."
                    ));
        }

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", ex.getMessage()));
    }

    private boolean isRateLimitMessage(String message) {
        if (message == null) {
            return false;
        }

        String normalized = message.toLowerCase();
        return normalized.contains("rate limit") || normalized.contains("please consider spreading out");
    }
}

