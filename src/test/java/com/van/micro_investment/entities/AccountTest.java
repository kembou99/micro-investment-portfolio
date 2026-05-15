package com.van.micro_investment.entities;

import com.van.micro_investment.enums.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Account – méthodes métier")
class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setBalance(new BigDecimal("100.0000"));
        account.setStatus(AccountStatus.ACTIVE);
        account.setName("Test Account");
    }

    // ─────────────────── credit ───────────────────

    @Nested
    @DisplayName("credit()")
    class CreditTests {

        @Test
        @DisplayName("crédite correctement le solde")
        void credit_addsAmountToBalance() {
            account.credit(new BigDecimal("50.00"));

            assertThat(account.getBalance()).isEqualByComparingTo("150.00");
        }

        @Test
        @DisplayName("cumule plusieurs crédits")
        void credit_multipleCredits_accumulatesCorrectly() {
            account.credit(new BigDecimal("10.00"));
            account.credit(new BigDecimal("20.00"));

            assertThat(account.getBalance()).isEqualByComparingTo("130.00");
        }

        @Test
        @DisplayName("lève IllegalArgumentException si montant = 0")
        void credit_zeroAmount_throwsIllegalArgument() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> account.credit(BigDecimal.ZERO))
                    .withMessageContaining("positive");
        }

        @Test
        @DisplayName("lève IllegalArgumentException si montant négatif")
        void credit_negativeAmount_throwsIllegalArgument() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> account.credit(new BigDecimal("-1.00")))
                    .withMessageContaining("positive");
        }
    }

    // ─────────────────── debit ────────────────────

    @Nested
    @DisplayName("debit()")
    class DebitTests {

        @Test
        @DisplayName("débite correctement le solde")
        void debit_subtractsAmountFromBalance() {
            account.debit(new BigDecimal("40.00"));

            assertThat(account.getBalance()).isEqualByComparingTo("60.00");
        }

        @Test
        @DisplayName("débit exact (solde à 0) est autorisé")
        void debit_exactBalance_setsBalanceToZero() {
            account.debit(new BigDecimal("100.00"));

            assertThat(account.getBalance()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("lève IllegalStateException si solde insuffisant")
        void debit_insufficientFunds_throwsIllegalState() {
            assertThatIllegalStateException()
                    .isThrownBy(() -> account.debit(new BigDecimal("200.00")))
                    .withMessageContaining("Insufficient");
        }

        @Test
        @DisplayName("lève IllegalArgumentException si montant = 0")
        void debit_zeroAmount_throwsIllegalArgument() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> account.debit(BigDecimal.ZERO))
                    .withMessageContaining("positive");
        }

        @Test
        @DisplayName("lève IllegalArgumentException si montant négatif")
        void debit_negativeAmount_throwsIllegalArgument() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> account.debit(new BigDecimal("-5.00")))
                    .withMessageContaining("positive");
        }
    }

    // ─────────────────── isActive ─────────────────

    @Nested
    @DisplayName("isActive()")
    class IsActiveTests {

        @Test
        @DisplayName("retourne true pour un compte ACTIVE")
        void isActive_activeAccount_returnsTrue() {
            account.setStatus(AccountStatus.ACTIVE);
            assertThat(account.isActive()).isTrue();
        }

        @Test
        @DisplayName("retourne false pour un compte INACTIVE")
        void isActive_inactiveAccount_returnsFalse() {
            account.setStatus(AccountStatus.INACTIVE);
            assertThat(account.isActive()).isFalse();
        }

        @Test
        @DisplayName("retourne false pour un compte CLOSED")
        void isActive_closedAccount_returnsFalse() {
            account.setStatus(AccountStatus.CLOSED);
            assertThat(account.isActive()).isFalse();
        }
    }
}
