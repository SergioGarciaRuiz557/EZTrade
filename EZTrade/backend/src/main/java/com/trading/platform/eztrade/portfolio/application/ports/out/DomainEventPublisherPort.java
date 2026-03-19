package com.trading.platform.eztrade.portfolio.application.ports.out;

public interface DomainEventPublisherPort {

    void publish(Object event);
}

