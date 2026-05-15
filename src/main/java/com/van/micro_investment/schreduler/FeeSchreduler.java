package com.van.micro_investment.schreduler;

import com.van.micro_investment.entities.Account;
import com.van.micro_investment.entities.AccountTransaction;
import com.van.micro_investment.repositories.AccountRepository;
import com.van.micro_investment.repositories.AccountTransactionRepository;
import com.van.micro_investment.services.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeeSchreduler {
    private final AccountRepository accountRepository;
    private final AccountTransactionRepository accountTransactionRepository;

    /**
     * Prélève les frais de gestion mensuels sur tous les comptes actifs.
     * S'exécute le 1er de chaque mois à minuit.
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void applyMonthlyFees() {
        log.info("Starting monthly fee collection process...");
        var activeAccounts = accountRepository.findAllActiveForFeeDeduction();

        activeAccounts.forEach(account -> {
            try {
                account.debit(new BigDecimal("1.00"));
                var fee = AccountTransaction.fee(account);
                accountTransactionRepository.save(fee);
                accountRepository.save(account);
                log.info("Frais déduits pour le compte {}", account.getId());
            } catch (IllegalStateException e) {
                // Solde insuffisant — on logue mais on ne bloque pas les autres
                log.warn("Insufficient funds to cover the charges {}", account.getId());
            }
        });

        log.info("Monthly fee collection process completed.");
    }



}
