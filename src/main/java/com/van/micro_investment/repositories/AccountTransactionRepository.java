package com.van.micro_investment.repositories;

import com.van.micro_investment.entities.AccountTransaction;
import com.van.micro_investment.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, UUID>, JpaSpecificationExecutor<AccountTransaction> {
    /** Historique paginé de toutes les transactions d'un compte */
    Page<AccountTransaction> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);

    /** Filtrage par type si besoin */
    Page<AccountTransaction> findByAccountIdAndTypeOrderByCreatedAtDesc(
            UUID accountId, TransactionType type, Pageable pageable);
}