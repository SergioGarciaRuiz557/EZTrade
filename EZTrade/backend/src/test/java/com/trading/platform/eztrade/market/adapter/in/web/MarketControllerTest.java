package com.trading.platform.eztrade.market.adapter.in.web;

import com.trading.platform.eztrade.market.application.ports.in.GetOverviewUserCase;
import com.trading.platform.eztrade.market.application.ports.in.GetPriceUserCase;
import com.trading.platform.eztrade.market.application.ports.in.SearchInstrumentUserCase;
import com.trading.platform.eztrade.market.domain.Instrument;
import com.trading.platform.eztrade.market.domain.InstrumentOverview;
import com.trading.platform.eztrade.market.domain.MarketPrice;
import com.trading.platform.eztrade.market.domain.Symbol;
import com.trading.platform.eztrade.security.configuration.BeansConfig;
import com.trading.platform.eztrade.security.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MarketController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MarketControllerTest.TestConfig.class)
class MarketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GetPriceUserCase getPriceUserCase;

    @Autowired
    private SearchInstrumentUserCase searchInstrumentUserCase;

    @Autowired
    private GetOverviewUserCase getOverviewUserCase;



    @Test
    @DisplayName("GET /api/v1/market/get-price devuelve el precio de mercado para el símbolo indicado")
    void get_price_returns_market_price_for_symbol() throws Exception {
        Symbol symbol = new Symbol("IBM");
        MarketPrice price = new MarketPrice(symbol, 150.5, Instant.parse("2026-03-04T10:15:30Z"));
        given(getPriceUserCase.getPrice(symbol)).willReturn(price);

        mockMvc.perform(get("/api/v1/market/get-price").param("symbol", symbol.value()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.symbol.value", is("IBM")))
                .andExpect(jsonPath("$.price", is(150.5)))
                .andExpect(jsonPath("$.timestamp", is("2026-03-04T10:15:30Z")));
    }

    @Test
    @DisplayName("GET /api/v1/market/search devuelve la lista de instrumentos que coinciden con el texto")
    void search_returns_instrument_list() throws Exception {
        List<Instrument> instruments = List.of(
                new Instrument("IBM", "International Business Machines", "United States", "USD"),
                new Instrument("IBM.MX", "International Business Machines", "Mexico", "MXN")
        );
        given(searchInstrumentUserCase.searchInstruments("IBM")).willReturn(instruments);

        mockMvc.perform(get("/api/v1/market/search").param("input", "IBM"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].symbol", is("IBM")))
                .andExpect(jsonPath("$[0].name", is("International Business Machines")))
                .andExpect(jsonPath("$[0].region", is("United States")))
                .andExpect(jsonPath("$[0].currency", is("USD")));
    }

    @Test
    @DisplayName("GET /api/v1/market/get-overview devuelve el overview del instrumento")
    void get_overview_returns_instrument_overview() throws Exception {
        Symbol symbol = new Symbol("IBM");
        InstrumentOverview overview = new InstrumentOverview(
                "IBM",
                "International Business Machines",
                "Technology",
                "Information Technology Services",
                1000000000L,
                15.5
        );
        given(getOverviewUserCase.getOverview(symbol)).willReturn(overview);

        mockMvc.perform(get("/api/v1/market/get-overview").param("symbol", symbol.value()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.symbol", is("IBM")))
                .andExpect(jsonPath("$.name", is("International Business Machines")))
                .andExpect(jsonPath("$.sector", is("Technology")))
                .andExpect(jsonPath("$.industry", is("Information Technology Services")))
                .andExpect(jsonPath("$.marketCap", is(1000000000)))
                .andExpect(jsonPath("$.peRatio", is(15.5)));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        GetPriceUserCase getPriceUserCase() {
            return mock(GetPriceUserCase.class);
        }

        @Bean
        SearchInstrumentUserCase searchInstrumentUserCase() {
            return mock(SearchInstrumentUserCase.class);
        }

        @Bean
        GetOverviewUserCase getOverviewUserCase() {
            return mock(GetOverviewUserCase.class);
        }

        @Bean
        AuthService authService() {
            return Mockito.mock(AuthService.class);
        }

        @Bean
        BeansConfig.SecurityPermissionEvaluator securityPermissionEvaluator() {
            return Mockito.mock(BeansConfig.SecurityPermissionEvaluator.class);
        }
    }
}

