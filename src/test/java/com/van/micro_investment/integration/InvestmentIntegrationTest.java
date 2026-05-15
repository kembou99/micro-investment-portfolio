package com.van.micro_investment.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.van.micro_investment.TestcontainersConfiguration;
import com.van.micro_investment.dto.requests.CreateAccountRequest;
import com.van.micro_investment.dto.requests.CreateUserRequest;
import com.van.micro_investment.dto.requests.RecordPurchaseRequest;
import com.van.micro_investment.dto.responses.AccountResponse;
import com.van.micro_investment.dto.responses.UserResponse;
import com.van.micro_investment.repositories.AccountRepository;
import com.van.micro_investment.repositories.AccountTransactionRepository;
import com.van.micro_investment.repositories.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@WithMockUser
@DisplayName("Integration Test: Investment Flow")
public class InvestmentIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserAccountRepository userRepository;

        @Autowired
        private AccountRepository accountRepository;

        @Autowired
        private AccountTransactionRepository transactionRepository;

        @BeforeEach
        void cleanDatabase() {
                transactionRepository.deleteAll();
                accountRepository.deleteAll();
                userRepository.deleteAll();
        }

        @Test
        @DisplayName("Should complete a full investment flow: create user -> create account -> record purchase -> check balance")
        void fullInvestmentFlow() throws Exception {
                // 1. Create User
                CreateUserRequest userRequest = new CreateUserRequest("john.doe@example.com", "John Doe");
                MvcResult userResult = mockMvc.perform(post("/api/v1/users")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                                .andReturn();

                UserResponse userResponse = objectMapper.readValue(userResult.getResponse().getContentAsString(),
                                UserResponse.class);
                UUID userId = userResponse.id();

                // 2. Create Account
                CreateAccountRequest accountRequest = new CreateAccountRequest("Main Savings", true, userId);
                MvcResult accountResult = mockMvc.perform(post("/api/v1/accounts")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(accountRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.name").value("Main Savings"))
                                .andExpect(jsonPath("$.isDefault").value(true))
                                .andReturn();

                AccountResponse accountResponse = objectMapper
                                .readValue(accountResult.getResponse().getContentAsString(), AccountResponse.class);
                UUID accountId = accountResponse.id();

                // 3. Record Purchase (e.g., $9.75) -> should trigger $0.25 round-up investment
                RecordPurchaseRequest purchaseRequest = new RecordPurchaseRequest(new BigDecimal("9.75"),
                                "Starbucks Coffee", accountId);
                mockMvc.perform(post("/api/v1/transactions/purchases")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(purchaseRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.amount").value(9.75))
                                .andExpect(jsonPath("$.roundUpAmount").value(0.25));

                // 4. Verify Account Balance (should be $0.25)
                mockMvc.perform(get("/api/v1/accounts/{accountId}/balance", accountId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.balance").value(0.25));

                // 5. Verify Transaction History
                mockMvc.perform(get("/api/v1/transactions/{accountId}/transactions", accountId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.totalElements").value(2)) // 1 PURCHASE + 1 INVESTMENT
                                .andExpect(jsonPath("$.content[0].type").value("INVESTMENT"))
                                .andExpect(jsonPath("$.content[0].amount").value(0.25))
                                .andExpect(jsonPath("$.content[1].type").value("PURCHASE"))
                                .andExpect(jsonPath("$.content[1].amount").value(9.75));
        }

        @Test
        @DisplayName("Should handle multiple purchases and accumulate round-ups")
        void multiplePurchasesFlow() throws Exception {
                // Setup: Create user and account
                UserResponse user = objectMapper.readValue(
                                mockMvc.perform(post("/api/v1/users").with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(
                                                                new CreateUserRequest("jane@example.com", "Jane Doe"))))
                                                .andReturn().getResponse().getContentAsString(),
                                UserResponse.class);

                AccountResponse account = objectMapper.readValue(
                                mockMvc.perform(post("/api/v1/accounts").with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(new CreateAccountRequest(
                                                                "Investment", true, user.id()))))
                                                .andReturn().getResponse().getContentAsString(),
                                AccountResponse.class);

                // Purchase 1: $12.40 -> $0.60 round-up
                mockMvc.perform(post("/api/v1/transactions/purchases").with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new RecordPurchaseRequest(
                                                new BigDecimal("12.40"), "Grocery", account.id()))))
                                .andExpect(status().isCreated());

                // Purchase 2: $3.10 -> $0.90 round-up
                mockMvc.perform(post("/api/v1/transactions/purchases").with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new RecordPurchaseRequest(
                                                new BigDecimal("3.10"), "Bus Ticket", account.id()))))
                                .andExpect(status().isCreated());

                // Total balance should be $1.50
                mockMvc.perform(get("/api/v1/accounts/{accountId}/balance", account.id()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.balance").value(1.50));
        }
}
