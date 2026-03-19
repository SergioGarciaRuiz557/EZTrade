package com.trading.platform.eztrade.wallet.application.ports.out;

public interface DomainEventPublisherPort {

    void publish(Object event);
}

