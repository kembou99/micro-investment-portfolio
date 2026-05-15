package com.van.micro_investment.entities;

import com.van.micro_investment.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;
import java.util.List;


@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@DynamicInsert
@Table(name = "accounts")
public class Account extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, precision = 19, scale = 4, columnDefinition = "default 0.0000")
    private BigDecimal balance=BigDecimal.ZERO;

    @Column(name = "is_default", nullable = false, columnDefinition = "default false")
    private boolean defaultAccount=false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "default 'ACTIVE'")
    private AccountStatus status=AccountStatus.ACTIVE;

    @Version
    @Column(nullable = false, columnDefinition = "default 0")
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private UserAccount user;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountTransaction> transactions;

    /**
     * Crédite le compte du montant donné.
     * Le @Version sera automatiquement incrémenté par JPA au moment du flush.
     */
    public void credit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("The amount to be credited must be positive.");
        }
        this.balance = this.balance.add(amount);
    }

    /**
     * Débite le compte du montant donné.
     * Vérifie que le solde est suffisant avant de débiter.
     */
    public void debit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("The amount to be debited must be positive.");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds.");
        }
        this.balance = this.balance.subtract(amount);
    }

    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }


}
