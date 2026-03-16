package com.trading.platform.eztrade.portfolio.adapter.out.persistence;

import com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa.CashAccountJpaEntity;
import com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa.SpringDataCashAccountRepository;
import com.trading.platform.eztrade.portfolio.application.ports.out.CashAccountRepositoryPort;
import com.trading.platform.eztrade.portfolio.domain.CashAccount;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CashAccountRepositoryAdapter implements CashAccountRepositoryPort {

    private final SpringDataCashAccountRepository repository;

    public CashAccountRepositoryAdapter(SpringDataCashAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<CashAccount> findByOwner(String owner) {
        return repository.findByOwner(owner).map(CashAccountMapper::toDomain);
    }

    @Override
    public CashAccount save(CashAccount cashAccount) {
        Optional<CashAccountJpaEntity> existing = repository.findByOwner(cashAccount.owner());
        CashAccountJpaEntity toSave = CashAccountMapper.toEntity(cashAccount);
        existing.ifPresent(entity -> toSave.setId(entity.getId()));
        return CashAccountMapper.toDomain(repository.save(toSave));
    }
}

