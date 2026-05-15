package com.van.micro_investment.services;

import com.van.micro_investment.dto.requests.CreateAccountRequest;
import com.van.micro_investment.entities.Account;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountService {
    public Account createAccount(CreateAccountRequest request);
    public List<Account> getByUserId(UUID id);
    public Account getById(UUID id);
    Account getByUserIdAndIsDefaultTrue(UUID userId);
    Account creditAccount(Account account, BigDecimal amount);
    Account debitAccount(Account account, BigDecimal amount);
}
