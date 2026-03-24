package com.trading.platform.eztrade.wallet.adapter.in.web;

import com.trading.platform.eztrade.wallet.domain.WalletDomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Traduce errores de dominio del wallet a respuestas HTTP de cliente.
 */
@RestControllerAdvice(basePackages = "com.trading.platform.eztrade.wallet.adapter.in.web")
public class WalletExceptionHandler {

    @ExceptionHandler(WalletDomainException.class)
    public ResponseEntity<Map<String, String>> handleWalletDomain(WalletDomainException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}

