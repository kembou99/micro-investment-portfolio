package com.van.micro_investment.services;

import com.van.micro_investment.dto.requests.CreateAccountRequest;
import com.van.micro_investment.entities.Account;
import com.van.micro_investment.entities.UserAccount;
import com.van.micro_investment.enums.AccountStatus;
import com.van.micro_investment.exceptions.ResourceNotFoundException;
import com.van.micro_investment.repositories.AccountRepository;
import com.van.micro_investment.services.impl.AccountServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountServiceImpl")
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AccountServiceImpl accountService;

    // ─────────────────── createAccount ────────────

    @Nested
    @DisplayName("createAccount()")
    class CreateAccountTests {

        @Test
        @DisplayName("crée un compte non-défaut sans retrait de l'ancien défaut")
        void createAccount_notDefault_savesWithoutUnmarkingPrevious() {
            UUID userId = UUID.randomUUID();
            UserAccount user = new UserAccount();
            user.setEmail("user@test.com");

            CreateAccountRequest request = new CreateAccountRequest("Savings", false, userId);

            when(userService.getById(userId)).thenReturn(user);

            Account saved = new Account();
            saved.setName("Savings");
            saved.setUser(user);
            saved.setDefaultAccount(false);

            when(accountRepository.saveAndFlush(any(Account.class))).thenReturn(saved);

            Account result = accountService.createAccount(request);

            assertThat(result.getName()).isEqualTo("Savings");
            assertThat(result.isDefaultAccount()).isFalse();
            verify(accountRepository, never()).findByUser_IdAndDefaultAccountTrue(any());
        }

        @Test
        @DisplayName("crée un compte défaut et démarque l'ancien défaut")
        void createAccount_isDefault_unmarksExistingDefault() {
            UUID userId = UUID.randomUUID();
            UserAccount user = new UserAccount();

            CreateAccountRequest request = new CreateAccountRequest("Main", true, userId);

            Account previousDefault = new Account();
            previousDefault.setDefaultAccount(true);

            when(userService.getById(userId)).thenReturn(user);
            when(accountRepository.findByUser_IdAndDefaultAccountTrue(userId))
                    .thenReturn(Optional.of(previousDefault));

            Account saved = new Account();
            saved.setName("Main");
            saved.setDefaultAccount(true);
            saved.setUser(user);

            when(accountRepository.saveAndFlush(any(Account.class))).thenReturn(saved);

            Account result = accountService.createAccount(request);

            assertThat(previousDefault.isDefaultAccount()).isFalse();
            assertThat(result.isDefaultAccount()).isTrue();
        }
    }

    // ─────────────────── getById ──────────────────

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("retourne le compte quand il existe")
        void getById_existingAccount_returnsAccount() {
            UUID id = UUID.randomUUID();
            Account account = new Account();
            when(accountRepository.findById(id)).thenReturn(Optional.of(account));

            Account result = accountService.getById(id);

            assertThat(result).isSameAs(account);
        }

        @Test
        @DisplayName("lève ResourceNotFoundException si le compte est introuvable")
        void getById_unknownId_throwsResourceNotFound() {
            UUID id = UUID.randomUUID();
            when(accountRepository.findById(id)).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> accountService.getById(id))
                    .withMessageContaining(id.toString());
        }
    }

    // ─────────────── getByUserIdAndIsDefaultTrue ──

    @Nested
    @DisplayName("getByUserIdAndIsDefaultTrue()")
    class GetDefaultAccountTests {

        @Test
        @DisplayName("retourne le compte défaut quand il existe")
        void getDefault_exists_returnsAccount() {
            UUID userId = UUID.randomUUID();
            Account def = new Account();
            def.setDefaultAccount(true);

            when(accountRepository.findByUser_IdAndDefaultAccountTrue(userId))
                    .thenReturn(Optional.of(def));

            Account result = accountService.getByUserIdAndIsDefaultTrue(userId);

            assertThat(result.isDefaultAccount()).isTrue();
        }

        @Test
        @DisplayName("lève ResourceNotFoundException si aucun compte défaut")
        void getDefault_notFound_throwsResourceNotFound() {
            UUID userId = UUID.randomUUID();
            when(accountRepository.findByUser_IdAndDefaultAccountTrue(userId))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> accountService.getByUserIdAndIsDefaultTrue(userId));
        }
    }

    // ─────────────────── creditAccount ────────────

    @Nested
    @DisplayName("creditAccount()")
    class CreditAccountTests {

        @Test
        @DisplayName("crédite et sauvegarde le compte")
        void creditAccount_validAmount_creditsAndSaves() {
            Account account = new Account();
            account.setBalance(new BigDecimal("50.0000"));
            account.setStatus(AccountStatus.ACTIVE);

            when(accountRepository.save(account)).thenReturn(account);

            Account result = accountService.creditAccount(account, new BigDecimal("25.00"));

            assertThat(result.getBalance()).isEqualByComparingTo("75.00");
            verify(accountRepository).save(account);
        }
    }

    // ─────────────────── debitAccount ─────────────

    @Nested
    @DisplayName("debitAccount()")
    class DebitAccountTests {

        @Test
        @DisplayName("débite et sauvegarde le compte")
        void debitAccount_validAmount_debitsAndSaves() {
            Account account = new Account();
            account.setBalance(new BigDecimal("100.0000"));
            account.setStatus(AccountStatus.ACTIVE);

            when(accountRepository.save(account)).thenReturn(account);

            Account result = accountService.debitAccount(account, new BigDecimal("30.00"));

            assertThat(result.getBalance()).isEqualByComparingTo("70.00");
            verify(accountRepository).save(account);
        }

        @Test
        @DisplayName("lève IllegalStateException si solde insuffisant")
        void debitAccount_insufficientFunds_throwsIllegalState() {
            Account account = new Account();
            account.setBalance(new BigDecimal("10.0000"));
            account.setStatus(AccountStatus.ACTIVE);

            assertThatIllegalStateException()
                    .isThrownBy(() -> accountService.debitAccount(account, new BigDecimal("50.00")))
                    .withMessageContaining("Insufficient");
        }
    }

    // ─────────────────── getByUserId ──────────────

    @Nested
    @DisplayName("getByUserId()")
    class GetByUserIdTests {

        @Test
        @DisplayName("retourne la liste des comptes de l'utilisateur")
        void getByUserId_returnsListOfAccounts() {
            UUID userId = UUID.randomUUID();
            Account a1 = new Account();
            Account a2 = new Account();

            when(accountRepository.findByUserId(userId)).thenReturn(List.of(a1, a2));

            List<Account> result = accountService.getByUserId(userId);

            assertThat(result).hasSize(2).containsExactly(a1, a2);
        }
    }
}
