package com.trading.platform.eztrade.wallet.adapter.out.persistence;

import com.trading.platform.eztrade.wallet.adapter.out.persistence.jpa.WalletTransactionJpaEntity;
import com.trading.platform.eztrade.wallet.domain.WalletTransaction;

/**
 * Mapper (paquete-privado) dominio &lt;-&gt; JPA para {@link WalletTransaction}.
 */
final class WalletTransactionMapper {

    private WalletTransactionMapper() {
    }

    static WalletTransaction toDomain(WalletTransactionJpaEntity entity) {
        return new WalletTransaction(
                entity.getId(),
                entity.getOwner(),
                entity.getMovementType(),
                entity.getAmount(),
                entity.getAvailableDelta(),
                entity.getReservedDelta(),
                entity.getAvailableBalanceAfter(),
                entity.getReservedBalanceAfter(),
                entity.getReferenceType(),
                entity.getReferenceId(),
                entity.getDescription(),
                entity.getOccurredAt()
        );
    }

    static WalletTransactionJpaEntity toEntity(WalletTransaction entry) {
        WalletTransactionJpaEntity entity = new WalletTransactionJpaEntity();
        entity.setId(entry.id());
        entity.setOwner(entry.owner());
        entity.setMovementType(entry.movementType());
        entity.setAmount(entry.amount());
        entity.setAvailableDelta(entry.availableDelta());
        entity.setReservedDelta(entry.reservedDelta());
        entity.setAvailableBalanceAfter(entry.availableBalanceAfter());
        entity.setReservedBalanceAfter(entry.reservedBalanceAfter());
        entity.setReferenceType(entry.referenceType());
        entity.setReferenceId(entry.referenceId());
        entity.setDescription(entry.description());
        entity.setOccurredAt(entry.occurredAt());
        return entity;
    }
}

