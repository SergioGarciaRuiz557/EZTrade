package com.trading.platform.eztrade.portfolio.adapter.in.web;

import com.trading.platform.eztrade.portfolio.adapter.in.web.dto.PortfolioResponse;
import com.trading.platform.eztrade.portfolio.application.ports.in.GetPortfolioUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador REST para consultar el portfolio del usuario autenticado.
 */
@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final GetPortfolioUseCase getPortfolioUseCase;

    public PortfolioController(GetPortfolioUseCase getPortfolioUseCase) {
        this.getPortfolioUseCase = getPortfolioUseCase;
    }

    @GetMapping
    public ResponseEntity<PortfolioResponse> getPortfolio(Authentication authentication) {
        return ResponseEntity.ok(PortfolioResponse.from(getPortfolioUseCase.getByOwner(authentication.getName())));
    }
}

