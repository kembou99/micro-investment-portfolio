package com.van.micro_investment.services.impl;

import com.van.micro_investment.dto.requests.RecordPurchaseRequest;
import com.van.micro_investment.dto.requests.WithdrawRequest;
import com.van.micro_investment.dto.responses.WithdrawalResponse;
import com.van.micro_investment.entities.AccountTransaction;
import com.van.micro_investment.repositories.AccountTransactionRepository;
import com.van.micro_investment.services.AccountService;
import com.van.micro_investment.services.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    private static final BigDecimal TAX_RATE = new BigDecimal("0.15");
    private final AccountTransactionRepository accountTransactionRepository;
    private final AccountService accountService;

    /**
     * Enregistre un achat ET investit le round-up dans la même transaction atomique.
     * @Retryable : si deux achats simultanés déclenchent un OptimisticLockException,
     * Spring réessaie automatiquement jusqu'à 3 fois avec un délai exponentiel.
     */
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )

    @Transactional
    public AccountTransaction recordPurchase(RecordPurchaseRequest request) {

        var account = accountService.getByUserIdAndIsDefaultTrue(getOwnerIdFromAccount(request.accountId()));

        // ── Calcul du round-up ────────────────────────────────
        BigDecimal roundUp = calculateRoundUp(request.amount());

        // ── 1. Enregistre l'achat ─────────────────────────────
        var purchase = AccountTransaction.purchase(account, request.amount(), roundUp, request.description());
        accountTransactionRepository.saveAndFlush(purchase);

        // ── 2. Investit le round-up (atomique) ────────────────
        if (roundUp.compareTo(BigDecimal.ZERO) > 0) {
            var investment = AccountTransaction.investment(account, roundUp);
            accountTransactionRepository.saveAndFlush(investment);
            accountService.creditAccount(account, roundUp);
        }

        return purchase;
    }

    /**
     * Retrait avec taxe de 15% sur les plus-values.
     */
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )

    @Transactional
    public WithdrawalResponse withdraw(WithdrawRequest request) {

        var account = accountService.getById(request.accountId());

        BigDecimal gross      = request.amount();
        BigDecimal tax        = gross.multiply(TAX_RATE).setScale(4, RoundingMode.HALF_UP);
        BigDecimal net        = gross.subtract(tax);

        accountService.debitAccount(account, gross);

        var tx = AccountTransaction.withdrawal(account, gross, tax, net);
        accountTransactionRepository.saveAndFlush(tx);


        return new WithdrawalResponse(tx.getId(), gross, tax, net);
    }

    /**
     * Historique paginé des transactions d'un compte.
     */
    public Page<AccountTransaction> getHistory(UUID accountId, int page, int size) {
         this.accountService.getById(accountId);
        return accountTransactionRepository
                .findByAccountIdOrderByCreatedAtDesc(accountId, PageRequest.of(page-1, size));

    }


    BigDecimal calculateRoundUp(BigDecimal amount) {
        BigDecimal ceiling = amount.setScale(0, RoundingMode.CEILING);
        return ceiling.subtract(amount).setScale(4, RoundingMode.HALF_UP);
    }

    private UUID getOwnerIdFromAccount(UUID accountId) {
        return  accountService.getById(accountId)
                .getUser().getId();
    }


}
