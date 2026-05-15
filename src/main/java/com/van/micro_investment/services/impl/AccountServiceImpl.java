package com.van.micro_investment.services.impl;

import com.van.micro_investment.dto.requests.CreateAccountRequest;
import com.van.micro_investment.entities.Account;
import com.van.micro_investment.entities.UserAccount;
import com.van.micro_investment.exceptions.ResourceNotFoundException;
import com.van.micro_investment.repositories.AccountRepository;
import com.van.micro_investment.services.AccountService;
import com.van.micro_investment.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserService userService;

    @Transactional
    public Account createAccount(CreateAccountRequest request) {
        UserAccount user = userService.getById(request.userId());
        // Si ce compte est marqué comme défaut, retirer l'ancien défaut
        if (request.isDefault()) {
            accountRepository.findByUser_IdAndDefaultAccountTrue(request.userId())
                    .ifPresent(existing -> existing.setDefaultAccount(false));
        }

        var account = new Account();
        account.setUser(user);
        account.setName(request.name());
        account.setDefaultAccount(request.isDefault());
        var accountSaved = accountRepository.save(account);
        log.info("Account created with id : {}, name : {}, default : {}, user id : {}, status : {}, balance: {}", accountSaved.getId(),
                accountSaved.getName(), accountSaved.isDefaultAccount(), accountSaved.getUser().getId(),accountSaved.getStatus(),accountSaved.getBalance());
        return accountSaved;

    }

    public List<Account> getByUserId(UUID userId) {
        return accountRepository.findByUserId(userId);
    }

    public Account getById(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID : " + accountId));
    }

    public Account getByUserIdAndIsDefaultTrue(UUID userId) {
        return accountRepository.findByUser_IdAndDefaultAccountTrue(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Default account not found for user with ID : " + userId));
    }

    @Override
    public Account creditAccount(Account account, BigDecimal amount) {
        account.credit(amount);
        var saved = accountRepository.save(account);
        log.info("Account credited with id: {},amount: {}", saved.getId(), amount);
        return saved;
    }

    @Override
    public Account debitAccount(Account account, BigDecimal amount) {
        account.debit(amount);
        var saved = accountRepository.save(account);
        log.info("Account debited with id: {},amount: {}", saved.getId(), amount);
        return saved;
    }
}
