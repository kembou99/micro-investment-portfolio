package com.van.micro_investment.entities;

import com.van.micro_investment.enums.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AccountTransaction – factory methods")
class AccountTransactionTest {

    private final Account account = new Account();

    @Test
    @DisplayName("purchase() crée une transaction de type PURCHASE avec le bon round-up")
    void purchase_createsCorrectTransaction() {
        BigDecimal amount  = new BigDecimal("9.75");
        BigDecimal roundUp = new BigDecimal("0.25");

        AccountTransaction tx = AccountTransaction.purchase(account, amount, roundUp, "Coffee");

        assertThat(tx.getType()).isEqualTo(TransactionType.PURCHASE);
        assertThat(tx.getAmount()).isEqualByComparingTo(amount);
        assertThat(tx.getRoundUpAmount()).isEqualByComparingTo(roundUp);
        assertThat(tx.getDescription()).isEqualTo("Coffee");
        assertThat(tx.getAccount()).isSameAs(account);
        // champs non applicables à un PURCHASE
        assertThat(tx.getTaxWithheld()).isNull();
        assertThat(tx.getNetAmount()).isNull();
    }

    @Test
    @DisplayName("investment() crée une transaction de type INVESTMENT avec description automatique")
    void investment_createsCorrectTransaction() {
        BigDecimal amount = new BigDecimal("0.25");

        AccountTransaction tx = AccountTransaction.investment(account, amount);

        assertThat(tx.getType()).isEqualTo(TransactionType.INVESTMENT);
        assertThat(tx.getAmount()).isEqualByComparingTo(amount);
        assertThat(tx.getDescription()).isEqualTo("Automatic rounding");
        assertThat(tx.getAccount()).isSameAs(account);
        assertThat(tx.getRoundUpAmount()).isNull();
    }

    @Test
    @DisplayName("fee() crée une transaction de type FEE avec un montant de 1.00")
    void fee_createsCorrectTransaction() {
        AccountTransaction tx = AccountTransaction.fee(account);

        assertThat(tx.getType()).isEqualTo(TransactionType.FEE);
        assertThat(tx.getAmount()).isEqualByComparingTo("1.00");
        assertThat(tx.getDescription()).isEqualTo("Monthly maintenance fee");
        assertThat(tx.getAccount()).isSameAs(account);
    }

    @Test
    @DisplayName("withdrawal() crée une transaction de type WITHDRAWAL avec taxe et montant net")
    void withdrawal_createsCorrectTransaction() {
        BigDecimal gross = new BigDecimal("100.00");
        BigDecimal tax   = new BigDecimal("15.00");
        BigDecimal net   = new BigDecimal("85.00");

        AccountTransaction tx = AccountTransaction.withdrawal(account, gross, tax, net);

        assertThat(tx.getType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(tx.getAmount()).isEqualByComparingTo(gross);
        assertThat(tx.getTaxWithheld()).isEqualByComparingTo(tax);
        assertThat(tx.getNetAmount()).isEqualByComparingTo(net);
        assertThat(tx.getDescription()).contains("15%");
        assertThat(tx.getRoundUpAmount()).isNull();
    }
}
