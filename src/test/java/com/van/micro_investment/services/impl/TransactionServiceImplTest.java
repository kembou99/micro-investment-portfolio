package com.van.micro_investment.services.impl;

import com.van.micro_investment.dto.requests.RecordPurchaseRequest;
import com.van.micro_investment.dto.requests.WithdrawRequest;
import com.van.micro_investment.dto.responses.WithdrawalResponse;
import com.van.micro_investment.entities.Account;
import com.van.micro_investment.entities.AccountTransaction;
import com.van.micro_investment.entities.UserAccount;
import com.van.micro_investment.enums.AccountStatus;
import com.van.micro_investment.enums.TransactionType;
import com.van.micro_investment.repositories.AccountTransactionRepository;
import com.van.micro_investment.services.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionServiceImpl")
class TransactionServiceImplTest {

    @Mock
    private AccountTransactionRepository accountTransactionRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    // ─────────────────── calculateRoundUp ─────────

    @Nested
    @DisplayName("calculateRoundUp() – logique de calcul")
    class CalculateRoundUpTests {

        @Test
        @DisplayName("9.75 → round-up = 0.25")
        void roundUp_partialCent_correctValue() {
            BigDecimal result = transactionService.calculateRoundUp(new BigDecimal("9.75"));
            assertThat(result).isEqualByComparingTo("0.2500");
        }

        @Test
        @DisplayName("montant entier → round-up = 0")
        void roundUp_wholeNumber_isZero() {
            BigDecimal result = transactionService.calculateRoundUp(new BigDecimal("10.00"));
            assertThat(result).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("3.01 → round-up = 0.99")
        void roundUp_smallDecimal_correctValue() {
            BigDecimal result = transactionService.calculateRoundUp(new BigDecimal("3.01"));
            assertThat(result).isEqualByComparingTo("0.9900");
        }

        @Test
        @DisplayName("0.50 → round-up = 0.50")
        void roundUp_halfUnit_correctValue() {
            BigDecimal result = transactionService.calculateRoundUp(new BigDecimal("0.50"));
            assertThat(result).isEqualByComparingTo("0.5000");
        }

        @Test
        @DisplayName("19.99 → round-up = 0.01")
        void roundUp_nearlyWholeNumber_correctValue() {
            BigDecimal result = transactionService.calculateRoundUp(new BigDecimal("19.99"));
            assertThat(result).isEqualByComparingTo("0.0100");
        }
    }

    // ─────────────────── recordPurchase ───────────

    @Nested
    @DisplayName("recordPurchase()")
    class RecordPurchaseTests {

        private Account account;
        private UUID accountId;
        private UUID userId;

        @BeforeEach
        void setUp() {
            accountId = UUID.randomUUID();
            userId    = UUID.randomUUID();

            UserAccount user = new UserAccount();
            user.setEmail("buyer@test.com");
            user.setId(userId); // nécessaire : user.getId() doit être non-null pour le stub any(UUID.class)

            account = new Account();
            account.setBalance(new BigDecimal("0.0000"));
            account.setStatus(AccountStatus.ACTIVE);
            account.setUser(user);
        }

        @Test
        @DisplayName("enregistre l'achat et investit le round-up quand celui-ci > 0")
        void recordPurchase_withRoundup_savesThreeRecords() {
            RecordPurchaseRequest request = new RecordPurchaseRequest(
                    new BigDecimal("9.75"), "Coffee", accountId);

            // getById pour récupérer l'owner
            when(accountService.getById(accountId)).thenReturn(account);
            // getByUserIdAndIsDefaultTrue retourne le même compte
            when(accountService.getByUserIdAndIsDefaultTrue(any(UUID.class))).thenReturn(account);
            // saveAndFlush retourne la tx
            when(accountTransactionRepository.saveAndFlush(any(AccountTransaction.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(accountService.creditAccount(any(Account.class), any(BigDecimal.class)))
                    .thenReturn(account);

            AccountTransaction result = transactionService.recordPurchase(request);

            assertThat(result.getType()).isEqualTo(TransactionType.PURCHASE);
            assertThat(result.getAmount()).isEqualByComparingTo("9.75");
            assertThat(result.getRoundUpAmount()).isEqualByComparingTo("0.25");

            // purchase + investment → 2 saveAndFlush
            verify(accountTransactionRepository, times(2)).saveAndFlush(any(AccountTransaction.class));
            verify(accountService).creditAccount(eq(account), any(BigDecimal.class));
        }

        @Test
        @DisplayName("n'investit pas si le montant est un entier (round-up = 0)")
        void recordPurchase_wholeAmount_noInvestment() {
            RecordPurchaseRequest request = new RecordPurchaseRequest(
                    new BigDecimal("10.00"), "Lunch", accountId);

            when(accountService.getById(accountId)).thenReturn(account);
            when(accountService.getByUserIdAndIsDefaultTrue(any(UUID.class))).thenReturn(account);
            when(accountTransactionRepository.saveAndFlush(any(AccountTransaction.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AccountTransaction result = transactionService.recordPurchase(request);

            assertThat(result.getRoundUpAmount()).isEqualByComparingTo("0.00");
            // 1 seul saveAndFlush (purchase uniquement)
            verify(accountTransactionRepository, times(1)).saveAndFlush(any(AccountTransaction.class));
            verify(accountService, never()).creditAccount(any(), any());
        }
    }

    // ─────────────────── withdraw ─────────────────

    @Nested
    @DisplayName("withdraw()")
    class WithdrawTests {

        @Test
        @DisplayName("calcule correctement la taxe à 15% et le montant net")
        void withdraw_calculatesCorrectTaxAndNet() {
            UUID accountId = UUID.randomUUID();
            BigDecimal gross = new BigDecimal("100.00");

            Account account = new Account();
            account.setBalance(new BigDecimal("200.0000"));
            account.setStatus(AccountStatus.ACTIVE);

            WithdrawRequest request = new WithdrawRequest(gross, accountId);

            when(accountService.getById(accountId)).thenReturn(account);
            when(accountService.debitAccount(any(Account.class), any(BigDecimal.class)))
                    .thenReturn(account);
            when(accountTransactionRepository.saveAndFlush(any(AccountTransaction.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            WithdrawalResponse response = transactionService.withdraw(request);

            BigDecimal expectedTax = gross.multiply(new BigDecimal("0.15"))
                    .setScale(4, RoundingMode.HALF_UP);
            BigDecimal expectedNet = gross.subtract(expectedTax);

            assertThat(response.grossAmount()).isEqualByComparingTo(gross);
            assertThat(response.taxWithheld()).isEqualByComparingTo(expectedTax);
            assertThat(response.netAmount()).isEqualByComparingTo(expectedNet);
        }

        @Test
        @DisplayName("appelle debitAccount avec le montant brut")
        void withdraw_callsDebitAccountWithGrossAmount() {
            UUID accountId = UUID.randomUUID();
            BigDecimal gross = new BigDecimal("50.00");

            Account account = new Account();
            account.setBalance(new BigDecimal("200.0000"));
            account.setStatus(AccountStatus.ACTIVE);

            when(accountService.getById(accountId)).thenReturn(account);
            when(accountService.debitAccount(any(Account.class), any(BigDecimal.class)))
                    .thenReturn(account);
            when(accountTransactionRepository.saveAndFlush(any(AccountTransaction.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            transactionService.withdraw(new WithdrawRequest(gross, accountId));

            verify(accountService).debitAccount(eq(account), eq(gross));
        }
    }

    // ─────────────────── getHistory ───────────────

    @Nested
    @DisplayName("getHistory()")
    class GetHistoryTests {

        @Test
        @DisplayName("retourne la page de transactions paginée")
        void getHistory_returnsPaginatedPage() {
            UUID accountId = UUID.randomUUID();
            Account account = new Account();
            AccountTransaction tx = AccountTransaction.investment(account, new BigDecimal("1.00"));

            Page<AccountTransaction> page = new PageImpl<>(List.of(tx));

            when(accountService.getById(accountId)).thenReturn(account);
            when(accountTransactionRepository.findByAccountIdOrderByCreatedAtDesc(
                    eq(accountId), any(PageRequest.class)))
                    .thenReturn(page);

            Page<AccountTransaction> result = transactionService.getHistory(accountId, 1, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isSameAs(tx);
        }

        @Test
        @DisplayName("vérifie que le compte existe avant de récupérer l'historique")
        void getHistory_verifiesAccountExists() {
            UUID accountId = UUID.randomUUID();
            Account account = new Account();

            when(accountService.getById(accountId)).thenReturn(account);
            when(accountTransactionRepository.findByAccountIdOrderByCreatedAtDesc(
                    eq(accountId), any(PageRequest.class)))
                    .thenReturn(Page.empty());

            transactionService.getHistory(accountId, 1, 10);

            verify(accountService).getById(accountId);
        }
    }
}
