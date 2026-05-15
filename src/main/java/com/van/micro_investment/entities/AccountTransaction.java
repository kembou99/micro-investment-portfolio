package com.van.micro_investment.entities;

import com.van.micro_investment.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@DynamicInsert
@Table(name = "transactions")
public class AccountTransaction extends BaseEntity{

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn( nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    /** Montant du round-up — renseigné uniquement pour PURCHASE */
    @Column(name = "round_up_amount", precision = 19, scale = 4)
    private BigDecimal roundUpAmount;

    /** Taxe retenue — renseignée uniquement pour WITHDRAWAL */
    @Column(name = "tax_withheld", precision = 19, scale = 4)
    private BigDecimal taxWithheld;

    /** Montant net après taxe — renseigné uniquement pour WITHDRAWAL */
    @Column(name = "net_amount", precision = 19, scale = 4)
    private BigDecimal netAmount;

    @Column(length = 500)
    private String description;

    public static AccountTransaction purchase(Account account, BigDecimal amount, BigDecimal roundUp, String desc) {
        var tx = new AccountTransaction();
        tx.account       = account;
        tx.type          = TransactionType.PURCHASE;
        tx.amount        = amount;
        tx.roundUpAmount = roundUp;
        tx.description   = desc;
        return tx;
    }

    public static AccountTransaction  investment(Account account, BigDecimal amount) {
        var tx = new AccountTransaction();
        tx.account     = account;
        tx.type        = TransactionType.INVESTMENT;
        tx.amount      = amount;
        tx.description = "Automatic rounding";
        return tx;
    }
    public static AccountTransaction fee(Account account) {
        var tx = new AccountTransaction();
        tx.account     = account;
        tx.type        = TransactionType.FEE;
        tx.amount      = new BigDecimal("1.00");
        tx.description = "Monthly maintenance fee";
        return tx;
    }

    public static AccountTransaction withdrawal(Account account, BigDecimal amount,
                                         BigDecimal taxWithheld, BigDecimal netAmount) {
        var tx = new AccountTransaction();
        tx.account     = account;
        tx.type        = TransactionType.WITHDRAWAL;
        tx.amount      = amount;
        tx.taxWithheld = taxWithheld;
        tx.netAmount   = netAmount;
        tx.description = "Withdrawal subject to capital gains tax (15%)";
        return tx;
    }
}
