package com.trading.platform.eztrade.portfolio.adapter.in.web;

import com.trading.platform.eztrade.portfolio.application.ports.in.GetPortfolioUseCase;
import com.trading.platform.eztrade.portfolio.domain.PortfolioSnapshot;
import com.trading.platform.eztrade.portfolio.domain.Position;
import com.trading.platform.eztrade.security.configuration.BeansConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PortfolioController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(PortfolioControllerTest.TestConfig.class)
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GetPortfolioUseCase getPortfolioUseCase;

    @Test
    @DisplayName("GET /api/portfolio devuelve la cartera del usuario autenticado")
    void getPortfolio_returnsOwnerSnapshot() throws Exception {
        Position position = Position.rehydrate(
                "demo@example.com",
                "AAPL",
                new BigDecimal("2"),
                new BigDecimal("150"),
                new BigDecimal("0"),
                java.time.LocalDateTime.of(2026, 3, 24, 12, 0)
        );

        PortfolioSnapshot snapshot = new PortfolioSnapshot(
                "demo@example.com",
                new BigDecimal("700"),
                new BigDecimal("300"),
                new BigDecimal("0"),
                List.of(position)
        );

        given(getPortfolioUseCase.getByOwner("demo@example.com")).willReturn(snapshot);

        mockMvc.perform(get("/api/portfolio").principal(auth("demo@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner").value("demo@example.com"))
                .andExpect(jsonPath("$.cashAvailable").value(700))
                .andExpect(jsonPath("$.totalCostBasis").value(300))
                .andExpect(jsonPath("$.totalRealizedPnl").value(0))
                .andExpect(jsonPath("$.positions[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$.positions[0].quantity").value(2))
                .andExpect(jsonPath("$.positions[0].averageCost").value(150));
    }

    @Test
    @DisplayName("GET /api/portfolio devuelve posiciones vacias cuando no hay cartera")
    void getPortfolio_returnsEmptyPositions() throws Exception {
        PortfolioSnapshot snapshot = new PortfolioSnapshot(
                "demo@example.com",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                List.of()
        );

        given(getPortfolioUseCase.getByOwner("demo@example.com")).willReturn(snapshot);

        mockMvc.perform(get("/api/portfolio").principal(auth("demo@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner").value("demo@example.com"))
                .andExpect(jsonPath("$.positions.length()").value(0));
    }

    private static Authentication auth(String owner) {
        return new UsernamePasswordAuthenticationToken(owner, null);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        GetPortfolioUseCase getPortfolioUseCase() {
            return Mockito.mock(GetPortfolioUseCase.class);
        }

        @Bean
        BeansConfig.SecurityPermissionEvaluator securityPermissionEvaluator() {
            return Mockito.mock(BeansConfig.SecurityPermissionEvaluator.class);
        }
    }
}

