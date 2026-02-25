package com.trading.platform.eztrade.market.adapter.out.external;

import com.trading.platform.eztrade.market.application.ports.out.GetPriceMarketProviderPort;
import com.trading.platform.eztrade.market.domain.ExternalApiException;
import com.trading.platform.eztrade.market.domain.MarketPrice;
import com.trading.platform.eztrade.market.domain.Symbol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class AlphaVantageAPI implements GetPriceMarketProviderPort {
    @Value("${alphaVantage.api.key}")
    private String apiKey;
    @Value("${alphaVantage.api.base-url}")
    private String baseUrl;
    @Value("${alphaVantage.api.timeout:5000}")
    private int timeout;


    private static final long THROTTLE_MS = 500L;
    private void throttle() {
        try {
            Thread.sleep(THROTTLE_MS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private static final String GLOBAL_QUOTE_FIELD = "Global Quote";


    @Override
    public MarketPrice getMarketPrice(Symbol symbol) {
        // Throttle to stay within free-tier rate limits (Alpha Vantage).
        throttle();

        // Alpha Vantage endpoint for real-time quote
        System.out.println("Getting market price for " + symbol);
        String url = String.format("%s?function=GLOBAL_QUOTE&symbol=%s&apikey=%s", baseUrl, symbol.value(), apiKey);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
        JsonNode responseJson;
        try {
            responseJson = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(JsonNode.class);
            if (responseJson == null || responseJson.get(GLOBAL_QUOTE_FIELD) == null || responseJson.get(GLOBAL_QUOTE_FIELD).get("05. price") == null) {
                throw new ExternalApiException("Invalid response from Alpha Vantage API: missing or malformed price data");
            }
        } catch (Exception e) {
            throw new ExternalApiException("Error communicating with Alpha Vantage API. Please check the value for alphaVantage.api.key in application.properties", e);
        }
        double currentPrice;
        try {
            currentPrice = Double.parseDouble(responseJson.get(GLOBAL_QUOTE_FIELD).get("05. price").asString());
        } catch (Exception e) {
            throw new ExternalApiException("Could not parse price from Alpha Vantage response", e);
        }
        return new MarketPrice(
                symbol,
                currentPrice,
                LocalDateTime.now().atZone(ZoneId.of("Europe/Madrid")).toInstant()
        );
    }
}
