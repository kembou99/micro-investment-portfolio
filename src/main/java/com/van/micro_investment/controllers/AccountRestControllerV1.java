package com.van.micro_investment.controllers;

import com.van.micro_investment.dto.requests.CreateAccountRequest;
import com.van.micro_investment.dto.responses.AccountBalanceResponse;
import com.van.micro_investment.dto.responses.AccountResponse;
import com.van.micro_investment.entities.Account;
import com.van.micro_investment.mappers.AccountMapper;
import com.van.micro_investment.services.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts")
public class AccountRestControllerV1 {
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {

        Account account = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountMapper.INSTANCE.toDto(account));
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<AccountBalanceResponse> getBalance(
            @PathVariable UUID accountId) {

        return ResponseEntity.ok(new AccountBalanceResponse(accountService.getById(accountId).getBalance()));
    }

}
