package com.trading.platform.eztrade.market.adapter.out.event;

import com.trading.platform.eztrade.market.application.ports.out.MarketPriceEventPublisherPort;
import com.trading.platform.eztrade.market.domain.event.MarketPriceUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publica eventos MarketPriceUpdatedEvent usando el ApplicationEventPublisher de Spring.
 */
@Component
public class SpringMarketPriceEventPublisherAdapter implements MarketPriceEventPublisherPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringMarketPriceEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(MarketPriceUpdatedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}

