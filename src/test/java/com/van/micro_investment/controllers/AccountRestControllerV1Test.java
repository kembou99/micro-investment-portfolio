package com.van.micro_investment.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.van.micro_investment.dto.requests.CreateAccountRequest;
import com.van.micro_investment.entities.Account;
import com.van.micro_investment.entities.UserAccount;
import com.van.micro_investment.enums.AccountStatus;
import com.van.micro_investment.exceptions.GlobalExceptionHandler;
import com.van.micro_investment.exceptions.ResourceNotFoundException;
import com.van.micro_investment.services.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountRestControllerV1.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser
@DisplayName("AccountRestControllerV1")
class AccountRestControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    // ─────────────────── POST /api/v1/accounts ────

    @Nested
    @DisplayName("POST /api/v1/accounts")
    class CreateAccountTests {

        @Test
        @DisplayName("201 – crée le compte avec des données valides")
        void createAccount_validRequest_returns201() throws Exception {
            UUID userId = UUID.randomUUID();
            CreateAccountRequest req = new CreateAccountRequest("Savings", true, userId);

            UserAccount user = new UserAccount();
            user.setEmail("user@test.com");

            Account account = new Account();
            account.setName("Savings");
            account.setDefaultAccount(true);
            account.setBalance(BigDecimal.ZERO);
            account.setStatus(AccountStatus.ACTIVE);
            account.setUser(user);

            when(accountService.createAccount(req)).thenReturn(account);

            mockMvc.perform(post("/api/v1/accounts")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Savings"))
                    .andExpect(jsonPath("$.isDefault").value("true"));
        }

        @Test
        @DisplayName("400 – nom de compte manquant")
        void createAccount_missingName_returns400() throws Exception {
            CreateAccountRequest req = new CreateAccountRequest("", false, UUID.randomUUID());

            mockMvc.perform(post("/api/v1/accounts")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 – userId manquant")
        void createAccount_missingUserId_returns400() throws Exception {
            // record avec userId null
            String body = "{\"name\":\"Savings\",\"isDefault\":false,\"userId\":null}";

            mockMvc.perform(post("/api/v1/accounts")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────── GET /api/v1/accounts/{id}/balance ──

    @Nested
    @DisplayName("GET /api/v1/accounts/{accountId}/balance")
    class GetBalanceTests {

        @Test
        @DisplayName("200 – retourne le solde du compte")
        void getBalance_existingAccount_returns200() throws Exception {
            UUID accountId = UUID.randomUUID();

            Account account = new Account();
            account.setBalance(new BigDecimal("150.5000"));
            account.setStatus(AccountStatus.ACTIVE);

            UserAccount user = new UserAccount();
            user.setEmail("user@test.com");
            account.setUser(user);

            when(accountService.getById(accountId)).thenReturn(account);

            mockMvc.perform(get("/api/v1/accounts/{accountId}/balance", accountId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(150.5));
        }

        @Test
        @DisplayName("404 – compte introuvable")
        void getBalance_unknownId_returns404() throws Exception {
            UUID accountId = UUID.randomUUID();
            when(accountService.getById(accountId))
                    .thenThrow(new ResourceNotFoundException("Account not found"));

            mockMvc.perform(get("/api/v1/accounts/{accountId}/balance", accountId))
                    .andExpect(status().isNotFound());
        }
    }
}
