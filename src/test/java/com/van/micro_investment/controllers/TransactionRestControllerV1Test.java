package com.van.micro_investment.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.van.micro_investment.dto.requests.RecordPurchaseRequest;
import com.van.micro_investment.dto.requests.WithdrawRequest;
import com.van.micro_investment.dto.responses.WithdrawalResponse;
import com.van.micro_investment.entities.Account;
import com.van.micro_investment.entities.AccountTransaction;
import com.van.micro_investment.enums.TransactionType;
import com.van.micro_investment.exceptions.GlobalExceptionHandler;
import com.van.micro_investment.exceptions.ResourceNotFoundException;
import com.van.micro_investment.services.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionRestControllerV1.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser
@DisplayName("TransactionRestControllerV1")
class TransactionRestControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    // ─────────── POST /api/v1/transactions/purchases ──

    @Nested
    @DisplayName("POST /api/v1/transactions/purchases")
    class RecordPurchaseTests {

        @Test
        @DisplayName("201 – enregistre un achat avec des données valides")
        void recordPurchase_validRequest_returns201() throws Exception {
            UUID accountId = UUID.randomUUID();
            RecordPurchaseRequest req = new RecordPurchaseRequest(
                    new BigDecimal("9.75"), "Coffee", accountId);

            Account account = new Account();
            AccountTransaction tx = AccountTransaction.purchase(
                    account, new BigDecimal("9.75"), new BigDecimal("0.25"), "Coffee");

            when(transactionService.recordPurchase(any(RecordPurchaseRequest.class))).thenReturn(tx);

            mockMvc.perform(post("/api/v1/transactions/purchases")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.type").value("PURCHASE"))
                    .andExpect(jsonPath("$.amount").value(9.75));
        }

        @Test
        @DisplayName("400 – montant manquant")
        void recordPurchase_missingAmount_returns400() throws Exception {
            String body = "{\"description\":\"Coffee\",\"accountId\":\"" + UUID.randomUUID() + "\"}";

            mockMvc.perform(post("/api/v1/transactions/purchases")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 – montant = 0 (inférieur au minimum)")
        void recordPurchase_zeroAmount_returns400() throws Exception {
            RecordPurchaseRequest req = new RecordPurchaseRequest(
                    BigDecimal.ZERO, "Coffee", UUID.randomUUID());

            mockMvc.perform(post("/api/v1/transactions/purchases")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 – description manquante")
        void recordPurchase_missingDescription_returns400() throws Exception {
            RecordPurchaseRequest req = new RecordPurchaseRequest(
                    new BigDecimal("9.75"), "", UUID.randomUUID());

            mockMvc.perform(post("/api/v1/transactions/purchases")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────── POST /api/v1/transactions/withdraw ──

    @Nested
    @DisplayName("POST /api/v1/transactions/withdraw")
    class WithdrawTests {

        @Test
        @DisplayName("200 – retrait avec les champs calculés")
        void withdraw_validRequest_returns200WithTaxDetails() throws Exception {
            UUID accountId = UUID.randomUUID();
            UUID txId      = UUID.randomUUID();
            WithdrawRequest req = new WithdrawRequest(new BigDecimal("100.00"), accountId);

            WithdrawalResponse response = new WithdrawalResponse(
                    txId,
                    new BigDecimal("100.00"),
                    new BigDecimal("15.0000"),
                    new BigDecimal("85.0000")
            );

            when(transactionService.withdraw(any(WithdrawRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/transactions/withdraw")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.grossAmount").value(100.0))
                    .andExpect(jsonPath("$.taxWithheld").value(15.0))
                    .andExpect(jsonPath("$.netAmount").value(85.0));
        }

        @Test
        @DisplayName("400 – accountId manquant")
        void withdraw_missingAccountId_returns400() throws Exception {
            String body = "{\"amount\":100.00}";

            mockMvc.perform(post("/api/v1/transactions/withdraw")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── GET /api/v1/transactions/{accountId}/transactions ──

    @Nested
    @DisplayName("GET /api/v1/transactions/{accountId}/transactions")
    class GetHistoryTests {

        @Test
        @DisplayName("200 – retourne une page de transactions")
        void getHistory_existingAccount_returnsPaginatedPage() throws Exception {
            UUID accountId = UUID.randomUUID();

            Account account = new Account();
            AccountTransaction tx = AccountTransaction.investment(account, new BigDecimal("0.25"));

            Page<AccountTransaction> page = new PageImpl<>(List.of(tx));

            when(transactionService.getHistory(any(UUID.class), anyInt(), anyInt())).thenReturn(page);

            mockMvc.perform(get("/api/v1/transactions/{accountId}/transactions", accountId)
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].type").value("INVESTMENT"));
        }

        @Test
        @DisplayName("404 – compte introuvable lors de la récupération de l'historique")
        void getHistory_unknownAccount_returns404() throws Exception {
            UUID accountId = UUID.randomUUID();
            when(transactionService.getHistory(any(UUID.class), anyInt(), anyInt()))
                    .thenThrow(new ResourceNotFoundException("Account not found"));

            mockMvc.perform(get("/api/v1/transactions/{accountId}/transactions", accountId))
                    .andExpect(status().isNotFound());
        }
    }
}
