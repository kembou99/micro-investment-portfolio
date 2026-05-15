package com.van.micro_investment.repositories;

import com.van.micro_investment.entities.Account;
import com.van.micro_investment.enums.AccountStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID>, JpaSpecificationExecutor<Account> {

    List<Account> findByUserId(UUID userId);

    List<Account> findByStatus(AccountStatus status);

    Optional<Account> findByUserIdAndDefaultAccountTrue(UUID userId);

    Optional<Account> findByUser_IdAndDefaultAccountTrue(UUID id);


    /**
     * Lecture avec PESSIMISTIC_WRITE uniquement pour le scheduler de fees
     * (traitement batch — pas de conflit attendu avec l'optimistic locking).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.status = 'ACTIVE'")
    List<Account> findAllActiveForFeeDeduction();

    /**
     * Vérifie si un compte appartient bien à un utilisateur donné.
     */
    boolean existsByIdAndUserId(UUID accountId, UUID userId);
}