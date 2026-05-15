package com.van.micro_investment.schreduler;

import com.van.micro_investment.entities.Account;
import com.van.micro_investment.entities.AccountTransaction;
import com.van.micro_investment.enums.AccountStatus;
import com.van.micro_investment.repositories.AccountRepository;
import com.van.micro_investment.repositories.AccountTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeeSchreduler")
class FeeSchredulerTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountTransactionRepository accountTransactionRepository;

    @InjectMocks
    private FeeSchreduler feeSchreduler;

    private Account activeAccount;

    @BeforeEach
    void setUp() {
        activeAccount = new Account();
        activeAccount.setBalance(new BigDecimal("10.0000"));
        activeAccount.setStatus(AccountStatus.ACTIVE);
        activeAccount.setName("Active Account");
    }

    @Test
    @DisplayName("déduit 1.00 € et enregistre une transaction FEE pour chaque compte actif")
    void applyMonthlyFees_activeAccounts_deductsAndSavesFee() {
        when(accountRepository.findAllActiveForFeeDeduction()).thenReturn(List.of(activeAccount));

        feeSchreduler.applyMonthlyFees();

        assertThat(activeAccount.getBalance()).isEqualByComparingTo("9.00");

        ArgumentCaptor<AccountTransaction> txCaptor = ArgumentCaptor.forClass(AccountTransaction.class);
        verify(accountTransactionRepository).save(txCaptor.capture());
        assertThat(txCaptor.getValue().getAmount()).isEqualByComparingTo("1.00");

        verify(accountRepository).save(activeAccount);
    }

    @Test
    @DisplayName("ne lève pas d'exception et continue si un compte a un solde insuffisant")
    void applyMonthlyFees_insufficientFunds_skipsAccountWithoutException() {
        Account poorAccount = new Account();
        poorAccount.setBalance(new BigDecimal("0.50"));
        poorAccount.setStatus(AccountStatus.ACTIVE);
        poorAccount.setName("Poor Account");

        Account richAccount = new Account();
        richAccount.setBalance(new BigDecimal("50.0000"));
        richAccount.setStatus(AccountStatus.ACTIVE);
        richAccount.setName("Rich Account");

        when(accountRepository.findAllActiveForFeeDeduction())
                .thenReturn(List.of(poorAccount, richAccount));

        // ne doit pas lever d'exception
        feeSchreduler.applyMonthlyFees();

        // seul le compte solide est sauvegardé
        verify(accountRepository, times(1)).save(richAccount);
        verify(accountRepository, never()).save(poorAccount);
        verify(accountTransactionRepository, times(1)).save(any(AccountTransaction.class));
    }

    @Test
    @DisplayName("ne fait rien si aucun compte actif")
    void applyMonthlyFees_noActiveAccounts_doesNothing() {
        when(accountRepository.findAllActiveForFeeDeduction()).thenReturn(List.of());

        feeSchreduler.applyMonthlyFees();

        verify(accountTransactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any(Account.class));
    }
}
