package com.trading.platform.eztrade.market.adapter.in.ws;

import com.trading.platform.eztrade.market.application.ports.in.SubscribeMarketDataUseCase;
import com.trading.platform.eztrade.market.domain.MarketPrice;
import jakarta.annotation.PostConstruct;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Publica precios de mercado en /topic/market para los clientes WebSocket.
 */
@Component
public class MarketWebSocketPublisher {

    private final SubscribeMarketDataUseCase subscribeMarketDataUseCase;
    private final SimpMessagingTemplate messagingTemplate;

    public MarketWebSocketPublisher(SubscribeMarketDataUseCase subscribeMarketDataUseCase,
                                    SimpMessagingTemplate messagingTemplate) {
        this.subscribeMarketDataUseCase = subscribeMarketDataUseCase;
        this.messagingTemplate = messagingTemplate;
    }

    @PostConstruct
    public void initSubscription() {
        subscribeMarketDataUseCase.subscribe(this::publishPrice);
    }

    private void publishPrice(MarketPrice price) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("instrument", price.getInstrument());
        payload.put("price", price.getPrice() != null ? price.getPrice() : BigDecimal.ZERO);
        payload.put("timestamp", price.getTimestamp() != null ? price.getTimestamp() : Instant.now());

        messagingTemplate.convertAndSend("/topic/market", (Object) payload);
    }
}

